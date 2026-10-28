package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;

/** Native PalletTown SignLady variables, separate from Oak's opening checkpoint. */
final class PalletSignLady {
    private final Database db;
    private final String owner;
    private boolean townLoaded, aside, ready;
    private int scene, x=5, y=15, direction=1;

    record View(int scene,boolean aside,boolean ready,int x,int y,int direction) {}
    View view(){return new View(scene,aside,ready,x,y,direction);}
    static View inspect(Database db,String owner){
        var vars=db.query("SELECT value FROM character_kanto_variables WHERE character_id=? AND variable=?",owner,"16496");
        var poses=db.query("SELECT x,y,direction FROM character_pallet_sign_lady WHERE character_id=?",owner);
        int scene=vars.isEmpty()?0:Integer.parseInt(vars.get(0)[0]);
        int x=poses.isEmpty()?5:Integer.parseInt(poses.get(0)[0]),y=poses.isEmpty()?15:Integer.parseInt(poses.get(0)[1]);
        int direction=poses.isEmpty()?1:Integer.parseInt(poses.get(0)[2]);
        return new View(scene,false,scene==0&&x==12&&y==2,x,y,direction);
    }
    static MapEntities.Npc project(MapEntities.Npc n,View view){
        if(view.scene()==2)return n;
        return new MapEntities.Npc(n.id(),n.graphics(),view.x(),view.y(),n.elevation(),view.direction(),
                view.direction()==1?7:view.direction()==0?8:view.direction()==2?9:10,0,0);
    }
    static String conversationNode(View view,int playerDirection){
        if(view.scene()==2)return "done";
        if(view.scene()==1)return "useful";
        if(view.ready())return "show";
        if(view.aside())return "read";
        return playerDirection==3?"first_right":"first_left";
    }

    PalletSignLady(Database db,String owner) {
        this.db=db;this.owner=owner;
        db.execute("INSERT OR IGNORE INTO character_pallet_sign_lady(character_id) VALUES (?)",owner);
        var row=db.query("SELECT x,y,direction FROM character_pallet_sign_lady WHERE character_id=?",owner).get(0);
        x=Integer.parseInt(row[0]);y=Integer.parseInt(row[1]);direction=Integer.parseInt(row[2]);
        var saved=db.query("SELECT value FROM character_kanto_variables WHERE character_id=? AND variable=?",owner,"16496");
        scene=saved.isEmpty()?0:Integer.parseInt(saved.get(0)[0]);
    }
    static void initialize(Database db) {
        db.execute("CREATE TABLE IF NOT EXISTS character_pallet_sign_lady (character_id TEXT PRIMARY KEY"
                +" REFERENCES characters(id) ON DELETE CASCADE,"
                +"x INTEGER NOT NULL DEFAULT 5,y INTEGER NOT NULL DEFAULT 15,direction INTEGER NOT NULL DEFAULT 1)");
    }
    void mapReady(StoryData.Request request,GameService.Position p,StoryData.State opening) {
        boolean town=p.bank()==3&&p.map()==0;
        if(!town){townLoaded=false;return;}
        if(!request.event().equals("MAP_READY")||townLoaded)return;
        townLoaded=true;aside=false;ready=false;
        // OnTransition: scene 1 becomes 2; an unfinished introduction is placed
        // at the sign, or at the route entrance after FLAG_SYS_POKEMON_GET.
        if(scene==1){scene=2;save();}
        else if(scene==0){
            boolean starterGiven=opening.enabled()&&opening.starter()!=3;
            x=starterGiven?12:5;y=starterGiven?2:15;direction=starterGiven?0:1;
            ready=starterGiven;save();
        }
    }
    MapEntities.Npc project(MapEntities.Npc n) {
        return project(n,view());
    }
    String node(StoryData.Request request,GameService.Position p) {
        if(p.bank()!=3||p.map()!=0)return null;
        int[] dx={0,0,-1,1},dy={1,-1,0,0};
        if(request.event().equals("BACKGROUND")) {
            var background=MapEntities.background(p,request.npc());
            if(background!=null && MapEntities.backgroundScript(3,0,background.id()).equals("PalletTown_EventScript_TrainerTips"))return "tips";
        }
        if(request.event().equals("COORDINATE")&&ready&&p.x()==13&&p.y()==2)return "trigger";
        if(scene==2)return null; // Completed NPC adjacency uses the source map's idle leash.
        if(!request.event().equals("NPC")||request.npc()!=0
                ||p.x()+dx[p.direction()]!=x||p.y()+dy[p.direction()]!=y)return null;
        return conversationNode(view(),p.direction());
    }
    int facePlayer(GameService.Position p){return p.x()!=x?(p.x()<x?2:3):p.y()<y?1:0;}
    void pose(GameService.Position target){x=target.x();y=target.y();direction=target.direction();save();}
    void movedAside(){aside=true;}
    void read(){scene=1;ready=false;save();}
    private void save(){
        db.transaction(()->{
            db.execute("UPDATE character_pallet_sign_lady SET x=?,y=?,direction=? WHERE character_id=?",x,y,direction,owner);
            db.execute("INSERT INTO character_kanto_variables(character_id,variable,value) VALUES (?,?,?)"
                    +" ON CONFLICT(character_id,variable) DO UPDATE SET value=excluded.value",owner,0x4070,scene);
        });
    }
}
