package top.totodile.pokemmo.client.ui.mail;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;

/** PcInteractionService.createMenu / InteractPacket.handlePcMenu, confirmed choices 1..4. */
public final class PcMenuWindow extends PlayerWindow {
    private final Button playerPc;

    public PcMenuWindow(
            Widget owner,
            ClientStrings strings,
            boolean mobile,
            Runnable pc,
            Runnable mail,
            Runnable onClose) {
        super(owner, mobile, "/mail-window", 340, 240, onClose);
        var panel = new DialogLayout();
        panel.setTheme("dialoglayout");
        var question = new Label(strings.get(2350));
        playerPc = new Button(strings.format(2351, "", ""));
        var trade = new Button(strings.get(2352));
        var mailbox = new Button(strings.get(2353));
        var cancel = new Button(strings.get(53));
        playerPc.addCallback(
                () -> {
                    closePopup();
                    pc.run();
                });
        trade.setEnabled(false);
        mailbox.addCallback(
                () -> {
                    closePopup();
                    mail.run();
                });
        cancel.addCallback(this::closePopup);
        panel.setHorizontalGroup(
                panel.createParallelGroup(question, playerPc, trade, mailbox, cancel));
        panel.setVerticalGroup(
                panel.createSequentialGroup(question, playerPc, trade, mailbox, cancel));
        frame.add(panel);
    }

    public void playerName(ClientStrings strings, String name) {
        playerPc.setText(strings.format(2351, "", name));
    }

    @Override
    protected void refresh() {}
}
