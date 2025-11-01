package myau.command.commands;

import myau.Myau;
import myau.command.Command;
import myau.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class FontCommand extends Command {

    public FontCommand() {
        super(new ArrayList<>(Arrays.asList("font", "fnt")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 2) {
            ChatUtil.sendFormatted(
                    String.format("%sUsage: .%s <&ofont_name&r> | .%s list&r | .%s status&r | .%s size <&osize&r>&r", Myau.clientName, args.get(0).toLowerCase(Locale.ROOT), args.get(0).toLowerCase(Locale.ROOT), args.get(0).toLowerCase(Locale.ROOT), args.get(0).toLowerCase(Locale.ROOT))
            );
            return;
        }

        String subCommand = args.get(1).toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "list":
                ChatUtil.sendFormatted(String.format("%sAvailable Fonts:&r %s&r", Myau.clientName, String.join(", ", Myau.fontManager.getLoadedFonts().keySet())));
                break;
            case "status":
                ChatUtil.sendFormatted(String.format("%sCurrent Font: &o%s&r (Size: %.1f)&r", Myau.clientName, Myau.fontManager.getCurrentFontName(), Myau.fontManager.getCurrentFontSize()));
                break;
            case "size":
                if (args.size() < 3) {
                    ChatUtil.sendFormatted(String.format("%sUsage: .%s size <&osize&r>&r", Myau.clientName, args.get(0).toLowerCase(Locale.ROOT)));
                    return;
                }
                try {
                    float size = Float.parseFloat(args.get(2));
                    Myau.fontManager.setFontSize(size);
                } catch (NumberFormatException e) {
                    ChatUtil.sendFormatted(String.format("%sInvalid font size value (&o%s&r)&r", Myau.clientName, args.get(2)));
                }
                break;
            default:
                String fontName = args.get(1);
                Myau.fontManager.setCurrentFont(fontName);
                break;
        }
    }
}
