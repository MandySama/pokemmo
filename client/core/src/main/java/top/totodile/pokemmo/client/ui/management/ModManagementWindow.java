package top.totodile.pokemmo.client.ui.management;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.ClientWindow;
import top.totodile.pokemmo.client.ui.common.ImportProgressDialog;
import top.totodile.pokemmo.client.ui.common.MessageDialog;
import top.totodile.pokemmo.client.ui.common.ActionConfirmDialog;
import top.totodile.pokemmo.client.resources.ModImporter;
import top.totodile.pokemmo.client.rom.RomFilePicker;

/** MOD 管理界面；导入、启用、删除与排序沿用参考流程。 */
public final class ModManagementWindow extends ClientWindow {
    private static final String ORDER_KEY="client.mods.order";
    private static final String ENABLED_KEY="client.mods.enabled_mods";
    private static final String VERBOSE_KEY="client.mods.debugs.verbose.enabled";
    private static final java.util.logging.Logger LOG=java.util.logging.Logger.getLogger(ModManagementWindow.class.getName());
    private final boolean mobile;
    private final Runnable onClose;
    private final java.util.function.Consumer<String> notification;
    private final java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground;
    private final Button save = new Button();
    private final ToggleButton verbose = new ToggleButton("    ");
    private final ClientStrings strings;
    private final ModImporter importer;
    private final RomFilePicker picker;
    private final top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer;
    private final top.totodile.pokemmo.client.config.ClientSettings preferences;
    private final java.util.List<ModListEntry> entries=new java.util.ArrayList<>();
    private final java.util.List<String> displayOrder=new java.util.ArrayList<>();
    private final java.util.Set<String> enabledMods=new java.util.LinkedHashSet<>();
    private boolean orderDirty;
    private boolean restartRequired;
    private Runnable restart=() -> com.badlogic.gdx.Gdx.app.exit();
    private final Button importMod;
    private final Button folder;
    private final DialogLayout list=new DialogLayout();
    private final ScrollPane scroll=new ScrollPane(list);
    private final MessageDialog messageDialog;
    private final ActionConfirmDialog deleteDialog;
    private final ImportProgressDialog progressDialog;
    private boolean busy, disposed;
    private boolean pointerHeld, pointerPanning;
    private int pointerX, pointerY, scrollX, scrollY;

    public void pointerDown(int x,int y) {
        pointerHeld=false;pointerPanning=false;
        if(mobile || !isOpen() || busy)return;
        Widget target=getGUI().getWidgetAt(x,y);
        while(target!=null && target!=scroll) {
            if(target==scroll.getVerticalScrollbar() || target==scroll.getHorizontalScrollbar())return;
            target=target.getParent();
        }
        if(target==null)return;
        pointerHeld=true;pointerX=x;pointerY=y;
        scrollX=scroll.getScrollPositionX();scrollY=scroll.getScrollPositionY();
    }
    public boolean pointerDrag(int x,int y) {
        if(!pointerHeld)return false;
        int dx=x-pointerX,dy=y-pointerY;
        if(!pointerPanning) {
            if(Math.abs(dx)<6 && Math.abs(dy)<6)return false;
            pointerPanning=true;
            // 沿用参考 pan 的按压取消流程，避免松开拖动时触发按钮。
            cancelPress(list);getGUI().handleMouse(-1,-1,Event.MOUSE_LBUTTON,false);cancelPress(list);
        }
        scroll.setScrollPositionX(Math.max(0,Math.min(scroll.getMaxScrollPosX(),scrollX-dx)));
        scroll.setScrollPositionY(Math.max(0,Math.min(scroll.getMaxScrollPosY(),scrollY-dy)));
        return true;
    }
    public boolean pointerUp() {
        boolean dragged=pointerPanning;pointerHeld=false;pointerPanning=false;return dragged;
    }
    private static void cancelPress(Widget widget) {
        if(widget instanceof Button button) {
            button.getModel().setArmed(false);button.getModel().setPressed(false);button.getModel().setHover(false);
        }
        for(int i=0;i<widget.getNumChildren();i++)cancelPress(widget.getChild(i));
    }
    private record ImportProgress(int stage,double value) {}
    private volatile ImportProgress importProgress=new ImportProgress(1095,-1);
    private final ResizableFrame frame = new ResizableFrame() {
        private int headerActionRight;
        @Override protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            if (mobile) headerActionRight = theme.getParameter("headerActionRight", 68);
        }
        @Override protected void paintBackground(GUI gui) {
            if (mobile) mobileBackground.accept(getBackground()); else super.paintBackground(gui);
        }
        @Override protected boolean isFrameElement(Widget child) {
            return mobile && child == save || super.isFrameElement(child);
        }
        @Override protected void layout() {
            super.layout();
            if (mobile) {
                save.adjustSize();
                save.setPosition(getInnerRight() - save.getWidth() - headerActionRight, getInnerY());
            }
        }
    };

    public ModManagementWindow(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose,
                        java.util.function.Consumer<de.matthiasmann.twl.renderer.Image> mobileBackground,
                        ModImporter importer, RomFilePicker picker,top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer,
                        top.totodile.pokemmo.client.config.ClientSettings preferences,
                        java.util.function.Consumer<String> notification) {
        super(owner);
        this.strings=strings;
        this.importer=importer;
        this.picker=picker;
        this.renderer=renderer;
        this.preferences=preferences;
        this.notification=notification;
        this.mobile = mobile;
        this.onClose = onClose;
        this.mobileBackground = mobileBackground;
        messageDialog=new MessageDialog(owner,strings,mobile,mobileBackground,() -> { if (isOpen()) raiseWindow(); });
        deleteDialog=new ActionConfirmDialog(owner,strings,mobile,mobileBackground,() -> { if(isOpen())raiseWindow(); });
        progressDialog=new ImportProgressDialog(owner,mobile,mobileBackground);
        frame.setTheme("/mods-panel");
        frame.setTitle(strings.get(1190));
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.addCloseCallback(this::closePopup);
        var content = new DialogLayout() {
            @Override public int getMinWidth() { return mobile ? super.getMinWidth() : Math.max(510, super.getMinWidth()); }
        };
        list.setTheme("settings-label-area");
        list.setAddDefaultGaps(false);
        resetOrder();
        updateList();
        var browse = new Button(strings.get(1191));
        importMod = new Button(strings.get(1225));
        importMod.addCallback(this::importMod);
        folder = new Button(strings.get(mobile ? 1154 : 1189));
        if(!mobile)folder.addCallback(this::openModDirectory);
        verbose.setTooltipContent(strings.get(1199));
        verbose.setTooltipDelay(150);
        var verboseLabel = new Label(strings.get(1198));
        verboseLabel.setTooltipContent(strings.get(1199));
        verboseLabel.setTooltipDelay(150);
        verboseLabel.setLabelFor(verbose);
        save.setText(strings.get(54));
        save.addCallback(this::saveChanges);
        var actions = content.createSequentialGroup(browse, importMod, folder)
                .addGap(0, 0, Short.MAX_VALUE).addWidget(verbose).addWidget(verboseLabel);
        var actionRow = content.createParallelGroup(browse, importMod, folder, verbose, verboseLabel);
        if (mobile) {
            save.setText("");
            save.setTheme("/mobile-save-icon");
            frame.add(save);
        } else actions.addGap(0, 0, Short.MAX_VALUE).addWidget(save);
        if (!mobile) actionRow.addWidget(save);
        content.setHorizontalGroup(content.createParallelGroup(scroll).addGroup(actions));
        var rows = content.createSequentialGroup(scroll).addGroup(actionRow);
        // 参考布局的末尾 5 像素属于纵向组，留在操作行下方。
        if (!mobile) rows.addGap(5);
        content.setVerticalGroup(rows);
        if (!mobile) content.setMinSize(510, 100);
        frame.add(content);
        add(frame);
    }

    public void open() {
        if (busy) { if(isOpen())raiseWindow();return; }
        pointerUp();messageDialog.closePopup();deleteDialog.closePopup();
        resetOrder();
        updateList();
        verbose.setActive(Boolean.parseBoolean(preferences.get(VERBOSE_KEY,"false")));
        boolean alreadyOpen=isOpen();
        if (alreadyOpen || openPopup()) {
            if(alreadyOpen)raiseWindow();
            positionWindow();
            scroll.setScrollPositionX(0);scroll.setScrollPositionY(0);
            frame.setHardVisible(true);
        }
    }

    public void positionWindow() {
        messageDialog.positionWindow();
        deleteDialog.positionWindow();
        if (progressDialog.isOpen()) {
            var progress=importProgress;
            progressDialog.update(strings.get(progress.stage()),progress.value());
        }
        if (!isOpen()) return;
        Widget owner = getOwner();
        if (mobile) {
            setPosition(owner.getInnerX(), owner.getInnerY());
            setSize(owner.getInnerWidth(), owner.getInnerHeight());
        } else {
            adjustSize();
            setPosition(owner.getInnerX() + (owner.getInnerWidth() - getWidth()) / 2,
                    owner.getInnerY() + (owner.getInnerHeight() - getHeight()) / 2);
        }
    }

    private void updateList() {
        list.removeAllChildren();
        entries.forEach(ModListEntry::close); entries.clear();
        var mods=new java.util.ArrayList<>(importer.list());
        var enabled=new java.util.ArrayList<>(enabledMods);
        mods.sort((a,b) -> {
            int firstOrder=displayOrder.indexOf(a.filename()),secondOrder=displayOrder.indexOf(b.filename());
            if(firstOrder>=0 && secondOrder>=0)return Integer.compare(firstOrder,secondOrder);
            if(firstOrder>=0 || secondOrder>=0)return firstOrder>=0?-1:1;
            int first=enabled.indexOf(a.filename()),second=enabled.indexOf(b.filename());
            if(first>=0 && second>=0)return Integer.compare(first,second);
            if(first>=0 || second>=0)return first>=0?-1:1;
            return b.filename().compareTo(a.filename());
        });
        displayOrder.clear();
        for(var mod:mods)displayOrder.add(mod.filename());
        // 空列表提示按参考居中；有条目时使用内容高度，避免将单个 MOD 拉满视口。
        scroll.setExpandContentSize(mods.isEmpty());
        if (mods.isEmpty()) {
            var empty=new Label(strings.get(1230));
            list.setHorizontalGroup(list.createSequentialGroup().addGap(0,0,Short.MAX_VALUE).addWidget(empty).addGap(0,0,Short.MAX_VALUE));
            list.setVerticalGroup(list.createSequentialGroup().addGap(0,0,Short.MAX_VALUE).addWidget(empty).addGap(0,0,Short.MAX_VALUE));
        } else {
            var columns=list.createParallelGroup();
            var rows=list.createSequentialGroup();
            for (var mod:mods) {
                byte[] png=null;
                try { png=importer.icon(mod); }
                catch(java.io.IOException error) { LOG.log(java.util.logging.Level.FINE,"Unable to read MOD icon",error); }
                ModListEntry row;
                int index=displayOrder.indexOf(mod.filename());
                boolean active=enabled.contains(mod.filename());
                Runnable up=() -> moveMod(mod.filename(),-1),down=() -> moveMod(mod.filename(),1);
                java.util.function.Consumer<Boolean> enable=value -> enableMod(mod.filename(),value);
                Runnable delete=() -> deleteDialog.show(strings.format(1229,mod.name()),() -> deleteMod(mod));
                try { row=new ModListEntry(mod,strings,png,renderer,active,index,mods.size(),up,down,enable,delete); }
                catch(RuntimeException error) {
                    LOG.log(java.util.logging.Level.WARNING,"Unable to decode MOD icon",error);
                    row=new ModListEntry(mod,strings,null,renderer,active,index,mods.size(),up,down,enable,delete);
                }
                entries.add(row);
                columns.addWidget(row);
                if(entries.size()>1)rows.addGap(DialogLayout.DEFAULT_GAP);
                rows.addWidget(row);
            }
            list.setHorizontalGroup(columns);
            list.setVerticalGroup(rows);
        }
    }

    private void resetOrder() {
        enabledMods.clear();
        for(String name:preferences.get(ENABLED_KEY,"").split("/"))if(!name.isBlank())enabledMods.add(name);
        displayOrder.clear();
        for(String name:preferences.get(ORDER_KEY,"").split("/")) {
            if(!name.isBlank() && !displayOrder.contains(name))displayOrder.add(name);
        }
        orderDirty=false;
    }

    private void moveMod(String filename,int direction) {
        int index=displayOrder.indexOf(filename),target=index+direction;
        if(index<0 || target<0 || target>=displayOrder.size())return;
        java.util.Collections.swap(displayOrder,index,target);
        orderDirty=true;
        updateList();
    }

    private void enableMod(String filename,boolean active) {
        if(busy || disposed)return;
        if(active)enabledMods.add(filename); else enabledMods.remove(filename);
        // 启停只改变状态，保持当前顺序；保存完整顺序，避免重开后按启用项重新排序。
        orderDirty=true;
        updateList();
    }

    private void deleteMod(ModImporter.Mod mod) {
        if(busy || disposed)return;
        try { importer.delete(mod); }
        catch(java.io.IOException | RuntimeException error) {
            LOG.log(java.util.logging.Level.WARNING,"Unable to delete MOD",error);
            messageDialog.show(strings.format(1094,mod.filename()));
            return;
        }
        enabledMods.remove(mod.filename()); displayOrder.remove(mod.filename());
        orderDirty=true; restartRequired=true;
        updateList();
        // 删除即生效，仅清理该文件的持久配置，不提交其他尚未保存的勾选。
        var changes=new java.util.HashMap<String,String>();
        for(String key:java.util.List.of(ENABLED_KEY,ORDER_KEY)) {
            String previous=preferences.get(key,"");
            String cleaned=java.util.Arrays.stream(previous.split("/"))
                    .filter(name -> !name.isBlank() && !name.equals(mod.filename())).collect(java.util.stream.Collectors.joining("/"));
            if(!previous.equals(cleaned))changes.put(key,cleaned);
        }
        if(!changes.isEmpty())try { preferences.save(changes); }
        catch(RuntimeException error) {
            LOG.log(java.util.logging.Level.WARNING,"Unable to save deleted MOD configuration",error);
            messageDialog.show(strings.get(87));
        }
    }

    private void saveChanges() {
        if(busy || disposed)return;
        String enabled=displayOrder.stream().filter(enabledMods::contains).collect(java.util.stream.Collectors.joining("/"));
        String previousEnabled=preferences.get(ENABLED_KEY,"");
        var changes=new java.util.HashMap<String,String>();
        if(!enabled.equals(previousEnabled))changes.put(ENABLED_KEY,enabled);
        if(orderDirty)changes.put(ORDER_KEY,String.join("/",displayOrder));
        String debug=Boolean.toString(verbose.isActive());
        if(!debug.equals(preferences.get(VERBOSE_KEY,"false")))changes.put(VERBOSE_KEY,debug);
        if(!changes.isEmpty()) {
            try { preferences.save(changes); }
            catch(RuntimeException error) {
                LOG.log(java.util.logging.Level.WARNING,"Unable to save MOD configuration",error);
                messageDialog.show(strings.get(87));
                return;
            }
            orderDirty=false;
        }
        restartRequired|=!enabled.equals(previousEnabled);
        closePopup();
    }

    public void onRestart(Runnable restart) { this.restart=java.util.Objects.requireNonNull(restart); }

    private void openModDirectory() {
        try {
            if(picker==null)throw new java.io.IOException("File browser unavailable");
            java.nio.file.Files.createDirectories(importer.directory());
            picker.openDirectory(importer.directory().toString(),error -> com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                if(!disposed)messageDialog.show(strings.format(1094,importer.directory().getFileName().toString()));
            }));
        }catch(java.io.IOException | RuntimeException error) {
            LOG.log(java.util.logging.Level.WARNING,"Unable to open MOD directory",error);
            messageDialog.show(strings.format(1094,importer.directory().getFileName().toString()));
        }
    }

    private void importMod() {
        if (busy || disposed) return;
        if (picker==null) { messageDialog.show(strings.get(1226)); return; }
        busy=true; importMod.setEnabled(false);
        try {
            picker.chooseMod(source -> com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                if (disposed) return;
                if (source==null) { finishImport(0); notification.accept(strings.get(1085)); return; }
                importProgress=new ImportProgress(1095,-1);
                progressDialog.show(strings.get(1095));
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    int result=1227;
                    try { importer.importMod(source,(stage,value) -> importProgress=new ImportProgress(stage,value)); }
                    catch (ModImporter.Failure error) {
                        LOG.log(java.util.logging.Level.FINE,"Unable to import MOD",error);
                        result=error.stringId;
                    } catch (Exception error) {
                        LOG.log(java.util.logging.Level.WARNING,"Unable to import MOD",error);
                        result=1226;
                    }
                    return result;
                }).whenComplete((message,error) -> {
                    // CompletableFuture 会保存 Error；必须从完成回调统一关闭进度框、恢复按钮。
                    if(error!=null)LOG.log(java.util.logging.Level.SEVERE,"MOD import task failed",error);
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> finishImport(error==null?message:1226,source.name()));
                });
            }),error -> com.badlogic.gdx.Gdx.app.postRunnable(() -> finishImport(1226)));
        } catch (RuntimeException error) { finishImport(1226); }
    }

    private void finishImport(int message) {
        finishImport(message,null);
    }

    private void finishImport(int message,String importedFilename) {
        if (disposed) return;
        progressDialog.closePopup();
        busy=false; importMod.setEnabled(true);
        if (message==1227) {
            // 参考 ModManagerWindow.Ir：成功导入即标记 rl，关闭管理窗口时提示重启。
            restartRequired=true;
            if(importedFilename!=null) {
                displayOrder.remove(importedFilename);
                displayOrder.add(0,importedFilename);
                orderDirty=true;
            }
            updateList();
            scroll.setScrollPositionX(0);scroll.setScrollPositionY(0);
        }
        if (message!=0) notification.accept(strings.get(message));
    }

    public boolean isImporting() { return busy; }
    public boolean closeMessage() {
        if(deleteDialog.isOpen()) { deleteDialog.closePopup(); return true; }
        if (!messageDialog.isOpen()) return false;
        messageDialog.closePopup(); return true;
    }
    public void dispose() {
        disposed=true; progressDialog.closePopup(); deleteDialog.closePopup(); messageDialog.closePopup();
        entries.forEach(ModListEntry::close); entries.clear();
    }

    @Override public void closePopup() {
        pointerUp();
        if(busy)return;
        if(!disposed && restartRequired) {
            messageDialog.show(strings.get(1179),() -> {
                if(disposed)return;
                try { restart.run(); }
                catch(RuntimeException error) {
                    LOG.log(java.util.logging.Level.WARNING,"Unable to restart client",error);
                    messageDialog.show(strings.get(2103));
                }
            });
            return;
        }
        boolean wasOpen = isOpen();
        super.closePopup();
        if (wasOpen) onClose.run();
    }
}
