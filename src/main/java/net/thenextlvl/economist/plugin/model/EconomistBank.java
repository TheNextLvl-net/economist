package net.thenextlvl.economist.plugin.model;

import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EconomistBank extends EconomistAccount implements Bank {
    private final String supportedCurrency;
    private final Set<UUID> members;
    private final String name;

    private volatile UUID owner;

    public EconomistBank(final String name, final UUID owner, final String supportedCurrency,
                         final BigDecimal balance, final Set<UUID> members) {
        super(owner, null, new ConcurrentHashMap<>(Map.of(supportedCurrency, balance)));
        this.name = name;
        this.owner = owner;
        this.supportedCurrency = supportedCurrency;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.addAll(members);
        this.members.remove(owner);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<UUID> getMembers() {
        return Set.copyOf(members);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean addMember(final UUID uuid) {
        return !owner.equals(uuid) && members.add(uuid);
    }

    @Override
    public boolean isMember(final UUID uuid) {
        return members.contains(uuid);
    }

    @Override
    public boolean removeMember(final UUID uuid) {
        return members.remove(uuid);
    }

    @Override
    public boolean setOwner(final UUID uuid) {
        if (owner.equals(uuid)) return false;
        members.remove(uuid);
        members.add(owner);
        owner = uuid;
        return true;
    }

    @Override
    public boolean canHold(final Currency currency) {
        return supportedCurrency.equals(currency.getName());
    }
}
