package the_fireplace.grandeconomy.econhandlers.ep;

import com.kamildanak.minecraft.enderpay.EnderPay;
import com.kamildanak.minecraft.enderpay.api.EnderPayApi;
import com.kamildanak.minecraft.enderpay.api.InsufficientCreditException;
import com.kamildanak.minecraft.enderpay.api.NoSuchAccountException;
import com.kamildanak.minecraft.enderpay.economy.Account;
import the_fireplace.grandeconomy.econhandlers.IEconHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class EnderPayEconHandler implements IEconHandler {
    @Override
    public long getBalance(UUID uuid, Boolean isPlayer) {
        try {
            return EnderPayApi.getBalance(uuid);
        } catch(NoSuchAccountException e) {
            return 0;
        }
    }

    @Override
    public boolean addToBalance(UUID uuid, long amount, Boolean isPlayer) {
        try {
            EnderPayApi.addToBalance(uuid, amount);
            return true;
        } catch(NoSuchAccountException ignored) {
            return false;
        }
    }

    @Override
    public boolean takeFromBalance(UUID uuid, long amount, Boolean isPlayer) {
        try {
            EnderPayApi.takeFromBalance(uuid, amount);
            return true;
        } catch(NoSuchAccountException | InsufficientCreditException e) {
            return false;
        }
    }

    @Override
    public boolean setBalance(UUID uuid, long amount, Boolean isPlayer) {
        try {
            EnderPayApi.takeFromBalance(uuid, EnderPayApi.getBalance(uuid));
            EnderPayApi.addToBalance(uuid, amount);
            return true;
        } catch(NoSuchAccountException | InsufficientCreditException e) {
            return false;
        }
    }

    @Override
    public String getCurrencyName(long amount) {
        return EnderPayApi.getCurrencyName(amount);
    }

    @Override
    public String toString(long amount) {
        return amount + ' ' + getCurrencyName(amount);
    }

    @Override
    public boolean ensureAccountExists(UUID uuid, Boolean isPlayer) {
        return Account.get(uuid) != null;
    }

    @Override
    public Boolean forceSave(UUID uuid, Boolean isPlayer) {
        try {
            Objects.requireNonNull(Account.get(uuid)).writeIfChanged();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    @Override
    public String getId() {
        return EnderPay.modID;
    }

    @Override
    public void init() {

    }
}
