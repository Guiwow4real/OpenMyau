package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.module.Category;
public class AntiDebuff extends Module {
    public final BooleanProperty blindness = new BooleanProperty("blindness", true);
    public final BooleanProperty nausea = new BooleanProperty("nausea", true);

    public AntiDebuff() {
        super("AntiDebuff","Remove Debuff",Category.WORLD,0, false,false);
    }
}
