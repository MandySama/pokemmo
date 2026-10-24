package top.totodile.pokemmo.client.resources;

import com.badlogic.gdx.files.FileHandle;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.rom.RomFilePicker;
import top.totodile.pokemmo.client.rom.RomManager;
import top.totodile.pokemmo.client.rom.RomType;

/** 参考 ChainedSearchPathFileResolver / ThemeModZipArchiveManager 的资源搜索与注册顺序。 */
public final class ClientResources implements Function<String, FileHandle>, AutoCloseable {
    private static final Logger LOG = Logger.getLogger(ClientResources.class.getName());
    public record Theme(String name, String path, boolean mobile, Function<String, FileHandle> files) {}
    private final Function<String, FileHandle> bundled;
    private final FileHandle writable;
    private final List<Function<String, FileHandle>> roots = new ArrayList<>();
    private final Map<java.nio.file.Path,ZipFile> archives = new LinkedHashMap<>();
    private final Map<java.nio.file.Path,Runnable> packageCleanup = new HashMap<>();
    private final List<ClientStrings.Source> stringSources = new ArrayList<>();
    private ClientStrings cachedStrings;
    private top.totodile.pokemmo.client.ui.character.CharacterAppearance appearance;
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private final Properties settings = new Properties();
    private final boolean mobile;
    private final ClientSettings preferences;
    private final RomManager roms;
    private final ModImporter modImporter;
    private final top.totodile.pokemmo.client.rom.nds.model.RegionModelCaches modelCaches;
    private RomFilePicker romFilePicker;

    public ClientResources(Function<String, FileHandle> bundled, FileHandle config, boolean mobile) {
        this.bundled = bundled;
        this.writable = config.parent().child("data");
        this.mobile = mobile;
        preferences = new ClientSettings(config);
        modImporter=new ModImporter(writable.child("mods").file().toPath(),this::releaseMod);
        roms = new RomManager(config.parent().child("roms").file().toPath(), new RomManager.Configuration() {
            @Override public String selectedFile(RomType type) {
                return preferences.get("client.roms." + type.id, "");
            }
            @Override public void selectFile(RomType type, String filename) {
                preferences.save(Map.of("client.roms." + type.id, filename));
            }
            @Override public void romImported(RomType type) {
                // ClientSettingsWindow.Le0：黑/白和白金成功加载即设置 oN，重复导入也适用。
                // RegionModelCacheManager 构造时据此令所有 NDS 地区缓存失效，启动完成后清除。
                if(!mobile && (type==RomType.BLACK_WHITE || type==RomType.PLATINUM))
                    preferences.save(Map.of("client.cache.model_cache.regenerate","true"));
            }
        });
        modelCaches=new top.totodile.pokemmo.client.rom.nds.model.RegionModelCaches(config.parent().child("cache").file().toPath(),preferences);
        settings.putAll(preferences.snapshot());
        roots.add(path -> writable.child(withoutData(path)));
        roots.add(path -> base(withoutData(path)));
        themes.put("default", new Theme("default", "data/themes/default/", false, this::global));
        themes.put("android", new Theme("android", "data/themes/android/", true, this::global));
        // 基础资源包先应用，启用的 MOD 依次应用；后者插入搜索链头部。
        FileHandle resources = base("resources.zip");
        if (!resources.exists()) resources = base("resources");
        if (resources.exists()) loadPackage(resources, true);
        String enabled = settings.getProperty("client.mods.enabled_mods", "");
        Set<String> names = new LinkedHashSet<>(Arrays.asList(enabled.split("/")));
        for (String name : names) {
            if (name.trim().isEmpty() || name.contains("\\") || name.equals("..")) continue;
            FileHandle file = writable.child("mods").child(name);
            if (!file.exists()) file = base("mods/" + name);
            if (file.exists()) loadPackage(file, false);
        }
        // 普通语言目录仍是内置来源；MOD 语言由 info.xml 的 strings 节注册。
        LinkedHashMap<String, FileHandle> baseStrings = new LinkedHashMap<>();
        for (FileHandle file : base("strings").list(".xml")) baseStrings.put(file.name(), file);
        for (FileHandle file : writable.child("strings").list(".xml")) baseStrings.put(file.name(), file);
        List<ClientStrings.Source> plain = baseStrings.values().stream()
                .map(file -> new ClientStrings.Source(file, true)).collect(java.util.stream.Collectors.toList());
        stringSources.addAll(0, plain);
    }

    private FileHandle base(String path) {
        try { return bundled.apply(path); }
        catch (IllegalStateException missing) { return writable.child(path); }
    }

    public RomManager roms() { return roms; }
    public top.totodile.pokemmo.client.rom.nds.model.RegionModelCaches modelCaches(){return modelCaches;}
    public ModImporter modImporter() { return modImporter; }
    public RomFilePicker romFilePicker() { return romFilePicker; }
    public void setRomFilePicker(RomFilePicker picker) { romFilePicker=picker; }

    private static String normalize(String path) {
        String normalized = Paths.get(path.replace('\\', '/')).normalize().toString().replace('\\', '/');
        if (normalized.equals("..") || normalized.startsWith("../") || normalized.startsWith("/") || normalized.contains(":"))
            throw new IllegalArgumentException("Invalid resource path: " + path);
        return normalized.equals(".") ? "" : normalized;
    }
    private static String withoutData(String path) {
        return path.startsWith("data/") ? path.substring(5) : path;
    }

    @Override public FileHandle apply(String path) { return global("data/" + normalize(path)); }

    private FileHandle global(String path) { return overlay(normalize(path), roots); }

    private static FileHandle overlay(String path, List<Function<String, FileHandle>> sources) {
        return new FileHandle(path) {
            private FileHandle first() {
                for (var source : sources) {
                    FileHandle candidate = source.apply(path);
                    if (candidate.exists()) return candidate;
                }
                throw new IllegalStateException("Resource not found: " + path);
            }
            @Override public boolean exists() { return sources.stream().anyMatch(source -> source.apply(path).exists()); }
            @Override public boolean isDirectory() { return exists() && first().isDirectory(); }
            @Override public InputStream read() { return first().read(); }
            @Override public long length() { return exists() ? first().length() : 0; }
            @Override public FileHandle child(String name) { return overlay(normalize(path.isEmpty()?name:path + "/" + name), sources); }
            @Override public FileHandle parent() {
                int end = path.lastIndexOf('/');
                return overlay(end < 0 ? "" : path.substring(0,end), sources);
            }
            @Override public FileHandle sibling(String name) { return parent().child(name); }
            @Override public FileHandle[] list() {
                Set<String> names = new LinkedHashSet<>();
                for (var source : sources) for (FileHandle file : source.apply(path).list()) names.add(file.name());
                return names.stream().map(this::child).toArray(FileHandle[]::new);
            }
            @Override public FileHandle[] list(String suffix) {
                return Arrays.stream(list()).filter(file -> file.name().endsWith(suffix)).toArray(FileHandle[]::new);
            }
        };
    }

    private Function<String, FileHandle> packageFiles(FileHandle file) throws IOException {
        if (file.isDirectory()) return path -> file.child(normalize(path));
        if (!file.name().toLowerCase(Locale.ROOT).endsWith(".zip") && !file.name().toLowerCase(Locale.ROOT).endsWith(".mod"))
            throw new IllegalArgumentException("Unsupported package extension");
        // APK 内置压缩包先复制到内部缓存；ZipFile 持续打开直到客户端释放资源。
        File source = file.file();
        if (!source.isFile()) {
            FileHandle cached = writable.parent().child("cache/resources/" + Integer.toHexString(file.path().hashCode()) + "-" + file.name());
            cached.parent().mkdirs();
            try (InputStream input = file.read(); OutputStream output = cached.write(false)) {
                byte[] buffer=new byte[8192];
                for(int length;(length=input.read(buffer))!=-1;)output.write(buffer,0,length);
            }
            source = cached.file();
        }
        ZipFile zip = new ZipFile(source);
        archives.put(source.toPath().toAbsolutePath().normalize(),zip);
        var entries=zip.entries();
        while(entries.hasMoreElements()){
            var entry=entries.nextElement();
            if(entry.isDirectory())break;
            if(entry.getName().contains("/"))throw new IllegalArgumentException("Flattened zip structure is unsupported");
        }
        return path -> zipFile(zip, normalize(path));
    }

    private static FileHandle zipFile(ZipFile zip, String path) {
        return new FileHandle(path) {
            @Override public boolean exists() { return zip.getEntry(path) != null || isDirectory(); }
            @Override public boolean isDirectory() {
                String prefix = path.isEmpty() ? "" : path + "/";
                return zip.stream().anyMatch(entry -> entry.getName().startsWith(prefix));
            }
            @Override public InputStream read() {
                var entry = zip.getEntry(path);
                if (entry == null || entry.isDirectory()) throw new IllegalStateException("Missing archive resource " + path);
                try { return zip.getInputStream(entry); }
                catch (IOException error) { throw new IllegalStateException(error); }
            }
            @Override public long length() { var entry=zip.getEntry(path); return entry == null ? 0 : entry.getSize(); }
            @Override public FileHandle child(String name) { return zipFile(zip, normalize(path.isEmpty()?name:path + "/" + name)); }
            @Override public FileHandle parent() { int end=path.lastIndexOf('/');return zipFile(zip,end<0?"":path.substring(0,end)); }
            @Override public FileHandle sibling(String name) { return parent().child(name); }
            @Override public FileHandle[] list() {
                String prefix=path.isEmpty()?"":path+"/";
                Set<String> children=new LinkedHashSet<>();
                zip.stream().filter(entry->entry.getName().startsWith(prefix)).forEach(entry->{
                    String rest=entry.getName().substring(prefix.length());
                    if (!rest.isEmpty()) children.add(rest.split("/",2)[0]);
                });
                return children.stream().map(this::child).toArray(FileHandle[]::new);
            }
            @Override public FileHandle[] list(String suffix) {
                return Arrays.stream(list()).filter(entry->entry.name().endsWith(suffix)).toArray(FileHandle[]::new);
            }
        };
    }

    private void loadPackage(FileHandle file, boolean builtIn) {
        try {
            var archiveFiles=packageFiles(file);
            boolean[] available={true};
            Function<String,FileHandle> files=path -> available[0]?archiveFiles.apply(path):missingFile(path);
            var factory = DocumentBuilderFactory.newInstance();
            // CompressedChunkBlockReader.yS tolerates Android parsers without these optional features.
            for(String feature:List.of("http://apache.org/xml/features/disallow-doctype-decl","http://xml.org/sax/features/external-general-entities","http://xml.org/sax/features/external-parameter-entities")){
                try {factory.setFeature(feature,feature.endsWith("disallow-doctype-decl"));}
                catch(javax.xml.parsers.ParserConfigurationException unsupported) {}
            }
            var builder=factory.newDocumentBuilder();
            builder.setEntityResolver((publicId,systemId)->new org.xml.sax.InputSource(new StringReader("")));
            Element resource;
            try (InputStream input=files.apply("info.xml").read()) {
                resource=builder.parse(input).getDocumentElement();
            }
            if (!resource.getTagName().equals("resource")) throw new IllegalArgumentException("Invalid mod metadata");
            List<Theme> declaredThemes=new ArrayList<>();
            List<ClientStrings.Source> declaredStrings=new ArrayList<>();
            var overlays=resource.getElementsByTagName("overlays");
            if(overlays.getLength()>1)throw new IllegalArgumentException("Duplicate overlays section");
            if(overlays.getLength()==1){
                var declared=((Element)overlays.item(0)).getElementsByTagName("overlay");
                for(int index=0;index<declared.getLength();index++){
                    Element entry=(Element)declared.item(index);
                    String path=normalize(entry.getAttribute("path"));
                    if(!entry.hasAttribute("path")||!files.apply(path).exists()||!files.apply(path).isDirectory())
                        throw new IllegalArgumentException("Invalid overlay path "+path);
                }
            }
            for (String sectionName : List.of("themes","strings")) {
                var sections=resource.getElementsByTagName(sectionName);
                if (sections.getLength()>1) throw new IllegalArgumentException("Duplicate " + sectionName + " section");
                if (sections.getLength()==0) continue;
                Element section=(Element)sections.item(0);
                var entries=section.getElementsByTagName(sectionName.equals("themes")?"theme":"string");
                for (int index=0;index<entries.getLength();index++) {
                    Element entry=(Element)entries.item(index);
                    String revision=section.hasAttribute(sectionName.equals("themes")?"theme_revision":"string_revision")
                            ? section.getAttribute(sectionName.equals("themes")?"theme_revision":"string_revision") : entry.getAttribute("revision");
                    int value=Integer.parseInt(revision), required=sectionName.equals("themes")?3:1;
                    if (value>required) throw new IllegalArgumentException("Unsupported " + sectionName + " revision " + value);
                    String path=normalize(entry.getAttribute("path"));
                    if(!entry.hasAttribute("path") || path.isEmpty())throw new IllegalArgumentException("Missing package path");
                    if (!files.apply(path).exists()) throw new IllegalArgumentException("Missing package path " + path);
                    if (sectionName.equals("strings")) {
                        var source=new ClientStrings.Source(files.apply(path),builtIn);
                        ClientStrings.validate(source);
                        if (value==required) declaredStrings.add(source);
                    } else {
                        String name=entry.getAttribute("name"), isMobile=entry.getAttribute("is_mobile");
                        if (name.trim().isEmpty() || name.equals("default") || name.equals("android") || themes.containsKey(name)
                                || declaredThemes.stream().anyMatch(theme->theme.name().equals(name)))
                            throw new IllegalArgumentException("Invalid or duplicate theme name " + name);
                        if (!isMobile.equals("true") && !isMobile.equals("false")) throw new IllegalArgumentException("Invalid is_mobile");
                        if (!files.apply(path).isDirectory() || !files.apply(path+"/theme.xml").exists()) throw new IllegalArgumentException("Missing theme.xml");
                        if (entry.hasAttribute("sprite_atlas") && !files.apply(normalize(entry.getAttribute("sprite_atlas"))).exists())
                            throw new IllegalArgumentException("Missing sprite atlas");
                        if (value==required && (!mobile || Boolean.parseBoolean(isMobile))) {
                            List<Function<String,FileHandle>> chain=new ArrayList<>();
                            chain.add(files);chain.addAll(roots);
                            declaredThemes.add(new Theme(name,path+"/",Boolean.parseBoolean(isMobile),p->overlay(p,chain)));
                        }
                    }
                }
            }
            // 参考 pd：道具图标位于包内 sprites/itemicons/，与 data/ 下的主题覆盖分别解析。
            Function<String,FileHandle> root=path -> {
                if(path.equals("data/sprites/itemicons")||path.startsWith("data/sprites/itemicons/")||path.equals("data/sprites/followsprites")||path.startsWith("data/sprites/followsprites/")) {
                    FileHandle icon=files.apply(withoutData(path));
                    if(icon.exists())return icon;
                }
                return files.apply(path);
            };
            roots.add(0,root);
            for (Theme theme:declaredThemes) themes.put(theme.name(),theme);
            stringSources.addAll(declaredStrings);
            if (!declaredStrings.isEmpty()) cachedStrings = null;
            if(!builtIn)packageCleanup.put(file.file().toPath().toAbsolutePath().normalize(),() -> {
                // 其他主题的继承链也可能引用此来源；删除后应继续查找基础资源，不能访问已关闭 ZIP。
                available[0]=false;
                roots.remove(root); stringSources.removeAll(declaredStrings);
                if (!declaredStrings.isEmpty()) cachedStrings = null;
                for(Theme theme:declaredThemes)themes.remove(theme.name(),theme);
            });
        } catch (Exception error) { LOG.log(Level.WARNING,"Error loading resource package " + file.path(),error); }
    }

    public ClientSettings preferences() { return preferences; }
    private static FileHandle missingFile(String path) {
        return new FileHandle(path) {
            @Override public boolean exists() { return false; }
            @Override public boolean isDirectory() { return false; }
            @Override public FileHandle[] list() { return new FileHandle[0]; }
            @Override public InputStream read() { throw new IllegalStateException("Removed MOD resource: "+path); }
        };
    }
    private void releaseMod(java.nio.file.Path path) throws IOException {
        ZipFile zip=archives.get(path);
        if(zip!=null) { zip.close(); archives.remove(path); }
        Runnable cleanup=packageCleanup.remove(path);
        if(cleanup!=null)cleanup.run();
    }
    public ClientStrings strings() {
        if (cachedStrings == null) cachedStrings = new ClientStrings(
                settings.getProperty("client.ui.chat.language.current_language"),
                stringSources.toArray(ClientStrings.Source[]::new));
        return cachedStrings;
    }
    /** PC 对齐 GdxAddonSpriteManager 的共享资源生命周期，Android 暂沿用原加载流程。 */
    private top.totodile.pokemmo.client.rom.nds.bw.BwFollowerSprites followerSprites;
    public synchronized top.totodile.pokemmo.client.rom.nds.bw.BwFollowerSprites followerSprites() throws java.io.IOException {
        if(followerSprites==null)followerSprites=new top.totodile.pokemmo.client.rom.nds.bw.BwFollowerSprites(roms().get(top.totodile.pokemmo.client.rom.RomType.BLACK_WHITE),this::apply);return followerSprites;
    }
    public synchronized top.totodile.pokemmo.client.ui.character.CharacterAppearance newAppearance() {
        if(mobile)return new top.totodile.pokemmo.client.ui.character.CharacterAppearance(apply("sprites/addons.pak"),strings());
        if(appearance==null)appearance=new top.totodile.pokemmo.client.ui.character.CharacterAppearance(apply("sprites/addons.pak"),strings());
        return appearance.copy();
    }
    public Theme selectedTheme() {
        Theme selected=themes.get(settings.getProperty("client.ui.theme",mobile?"android":"default"));
        return selected==null || (mobile && !selected.mobile()) ? defaultTheme() : selected;
    }
    public Theme defaultTheme() { return themes.get(mobile?"android":"default"); }
    public URL themeUrl(Theme theme) {
        try { return new URL(null,"asset:/"+theme.path()+"theme.xml",new AssetUrlStreamHandler(theme.files())); }
        catch (java.net.MalformedURLException error) { throw new IllegalArgumentException(error); }
    }
    public Map<String,String> imageConstants(Theme theme,String language) {
        Map<String,String> constants=new HashMap<>();
        String code=language.split("-")[0];
        for (String stem:List.of("effectivity_buttons","gameshop")) {
            String image="res/"+stem+"_"+code+".png";
            if (!theme.files().apply(theme.path()+image).exists() && !apply("themes/default/"+image).exists())
                image="res/"+stem+"_en.png";
            constants.put(stem+"_lang",image);
        }
        return constants;
    }
    @Override public void close() {
        if(appearance!=null){appearance.dispose();appearance=null;}
        modelCaches.close();
        for (ZipFile zip:archives.values()) try { zip.close(); }
        catch (IOException error) { LOG.log(Level.WARNING,"Cannot close resource package",error); }
        archives.clear(); packageCleanup.clear();
    }
}
