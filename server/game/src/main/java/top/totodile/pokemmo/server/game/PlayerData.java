package top.totodile.pokemmo.server.game;

import java.util.List;

/** Local equivalents of owned_item, trainer-card fields and MailService's list/detail records. */
public final class PlayerData {
    private PlayerData() {}
    /** PcInteractionService.isPcTarget's confirmed Kanto centre computer. */
    public static boolean pcTarget(GameService.Position p,int region){
        return p!=null&&region==0&&p.bank()==5&&p.map()==4&&p.x()==11&&(p.y()==1||p.y()==2)&&p.direction()==1;
    }
    public record Item(String objectId,int itemId,int amount,int region) {}
    public record Trainer(GameService.Character character,int money,int battlePoints,long playedSeconds,
                          int caught,int obedience,int badges) {}
    public record Mail(String id,String sender,String recipient,long createdAt,String subject,String body,boolean read) {}
    public record MailPage(List<Mail> entries,int total,int unread,int page,boolean sent) {
        public MailPage { entries=List.copyOf(entries); }
    }
    public enum MailResult { SUCCESS, INVALID_RECIPIENT, INVALID_SUBJECT, INVALID_BODY, NOT_FOUND, ACCESS_DENIED }
}
