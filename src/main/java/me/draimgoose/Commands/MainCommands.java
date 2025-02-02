package me.draimgoose.Commands;

import me.draimgoose.Config.MainConfig;
import me.draimgoose.Config.MessageConfig;
import me.draimgoose.DraimDonate;
import me.draimgoose.GUIs.AdminGUI;
import me.draimgoose.Utils.*;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class MainCommands implements CommandExecutor {
    private DraimDonate pl;

    /**
     * Instantiates a new Main commands.
     *
     * @param pl the plugin
     */
    public MainCommands(DraimDonate pl) {
        super();
        this.pl = pl;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getName().equals("draimdonate")) {
            final Player p = (Player)sender;
            if (args.length == 0) {
                for (final String s : MessageConfig.getMSG().getCFG().getStringList("Messages.Help")) {
                    p.sendMessage(ColorUtils.color(s));
                }
                return false;
            } final String lowerCase = args[0].toLowerCase();
            switch (lowerCase) {
                case "reload": {
                    if (p.hasPermission("draimdonate.reload")) {
                        MainConfig.getMain().reloadCFG();
                        DraimDonate.reloadToken();
                        MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Another.Plugin-Reload"), sender);
                        MessageUtils.sendLog(DraimDonate.getConfigString("Messages.Another.Plugin-Reload"));
                        SoundUtils.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                        break;
                    }
                    MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Another.No-Permission"), sender);
                    SoundUtils.playSound(p, Sound.BLOCK_ANVIL_PLACE);
                    break;
                }
                case "pay": {
                    if (args.length != 2 || !isNumeric(args[1])) {
                        MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Another.Arg-Error"), sender);
                        return true;
                    }
                    QiWiModule.generateBill(p, Integer.parseInt(args[1]));
                    MessageUtils.sendLog(MessageUtils.config("config","Messages.Console.PayLink", p, Integer.parseInt(args[1])));
                    SoundUtils.playSound(p, Sound.ENTITY_PLAYER_LEVELUP);
                    break;
                }
                case "check": {
                    if  (QiWiModule.getClients().containsKey(p.getUniqueId())) {
                        QiWiModule.checkBill(p);
                        break;
                    }
                    MessageUtils.sendUsefulMSG(p, "Messages.Another.NoBill");
                    SoundUtils.playSound(p, Sound.BLOCK_ANVIL_PLACE);
                    break;
                }
                case "reject": {
                    if (QiWiModule.getClients().containsKey(p.getUniqueId())) {
                        QiWiModule.getClients().remove(p.getUniqueId());
                        MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Bill.Reject"), sender);
                        MessageUtils.sendLog(MessageUtils.config("config", "Messages.Console.RejectLink", p, 0));
                        SoundUtils.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                        break;
                    }
                    MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Bill.No-Bill"), sender);
                    SoundUtils.playSound(p, Sound.BLOCK_ANVIL_PLACE);
                    break;
                }
                case "admin": {
                    if (p.hasPermission("draimdonate.admin")) {
                        p.openInventory(new AdminGUI().getMenu());
                        p.getPersistentDataContainer().set(pl.AdminGUI, PersistentDataType.STRING, AdminGUI.name);
                        return true;
                    }
                    MessageUtils.sendMessage(DraimDonate.getConfigString("Messages.Another.No-Permission"), sender);
                    SoundUtils.playSound(p, Sound.BLOCK_ANVIL_PLACE);
                    break;
                }
                default: {
                    p.sendMessage(MessageUtils.config("config","Messages.Another.NoArg", p, 0));
                    for (final String s2 : MessageConfig.getMSG().getCFG().getStringList("Messages.Help")) {
                        p.sendMessage(ColorUtils.color(s2));
                    }
                    SoundUtils.playSound(p, Sound.BLOCK_ANVIL_PLACE);
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Проверка на циферки, вместо буковок, а то бывают ебанаты, которые пишут /dn pay PIZDA,  а нужно /dn pay <сумма>
     * Is numeric boolean.
     *
     * @param str the string
     * @return the boolean
     */
    public static boolean isNumeric(final String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
