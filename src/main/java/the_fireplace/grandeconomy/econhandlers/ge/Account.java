package the_fireplace.grandeconomy.econhandlers.ge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.grandeconomy.GrandEconomy;
import the_fireplace.grandeconomy.TimeUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Account {
    private static HashMap<String, Account> objects = new HashMap<>();
    private static File location;
    private boolean changed;

    private UUID uuid;
    private long balance;
    private long lastLogin;
    private long lastCountActivity;
    private boolean isPlayer = false;

    private Account(UUID uuid) {
        this.uuid = uuid;
        this.balance = GrandEconomy.cfg.startBalance;
        long now = TimeUtils.getCurrentDay();
        this.lastLogin = now;
        this.lastCountActivity = now;
        this.changed = true;
        if(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid) != null)
            this.isPlayer = true;
    }

    public static Account get(EntityPlayer player) {
        return get(player.getUniqueID());
    }

    @Nullable
    public static Account get(UUID uuid) {
        Account account = objects.get(uuid.toString());
        if (account != null)
            return account;

        if (location == null)
            return null;
        //noinspection ResultOfMethodCallIgnored
        location.mkdirs();

        account = new Account(uuid);
        objects.put(uuid.toString(), account);

        File file = account.getFile();
        if (!file.exists()) return account;

        account.read();
        return account;
    }

    public static void clear() {
        Account.location = null;
        Account.objects.clear();
    }

    public static void setLocation(File location) {
        Account.location = location;
    }

    boolean update() {
        long now = TimeUtils.getCurrentDay();
        long activityDeltaDays = now - this.lastCountActivity;
        this.lastCountActivity = now;

        if (!isPlayer || activityDeltaDays == 0) return false;

        if (GrandEconomy.cfg.basicIncome && getPlayerMP() != null) {
            long loginDeltaDays = (now - this.lastLogin);
            if (loginDeltaDays > GrandEconomy.cfg.maxBasicIncomeDays)
                loginDeltaDays = GrandEconomy.cfg.maxBasicIncomeDays;
            this.lastLogin = now;
            this.balance += loginDeltaDays * GrandEconomy.cfg.basicIncomeAmount;
        }
        return activityDeltaDays > 0;
    }

    void writeIfChanged() throws IOException {
        if (changed) write();
    }

    private File getFile() {
        return new File(location, uuid + ".json");
    }

    private void read() {
        read(getFile());
    }

    private void read(File file) {
        changed = false;

        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(file));
            JsonObject jsonObject = (JsonObject) obj;
            balance = jsonObject.get("balance").getAsLong();
            lastLogin = jsonObject.get("lastLogin").getAsLong();
            lastCountActivity = jsonObject.get("lastCountActivity").getAsLong();
            if(jsonObject.has("isPlayer"))
                isPlayer = jsonObject.get("isPlayer").getAsBoolean();
            else if(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(uuid) != null) {
                this.isPlayer = true;
                this.changed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write() throws IOException {
        write(getFile());
    }

    private void write(File location) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("balance", balance);
        obj.addProperty("lastLogin", lastLogin);
        obj.addProperty("lastCountActivity", lastCountActivity);
        obj.addProperty("isPlayer", isPlayer);
        try (FileWriter file = new FileWriter(location)) {
            String str = obj.toString();
            file.write(str);
        }
        changed = false;
    }

    public long getBalance() {
        return balance;
    }

    void setBalance(long v, boolean showMsg) {
        if(balance != v) {
            balance = v;
            changed = true;
        }
        if(showMsg)
            Objects.requireNonNull(getPlayerMP()).sendMessage(new TextComponentTranslation("Your balance is now: %s", balance));
    }

    void addBalance(long v, boolean showMsg) {
        setBalance(balance + v, showMsg);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private EntityPlayerMP getPlayerMP() {
        return GrandEconomy.minecraftServer.getPlayerList().getPlayerByUUID(uuid);
    }
}
