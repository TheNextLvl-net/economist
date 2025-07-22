package net.thenextlvl.economist.service.model;

import net.thenextlvl.service.api.economy.bank.Bank;
import net.thenextlvl.service.api.economy.currency.Currency;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class BankDelegate extends AccountDelegate implements Bank {
    private final net.thenextlvl.economist.api.bank.Bank delegate;

    public BankDelegate(net.thenextlvl.economist.api.bank.Bank delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public @Unmodifiable Set<UUID> getMembers() {
        return delegate.getMembers();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean addMember(UUID uuid) {
        return delegate.addMember(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return delegate.isMember(uuid);
    }

    @Override
    public boolean removeMember(UUID uuid) {
        return delegate.removeMember(uuid);
    }

    @Override
    public boolean setOwner(UUID uuid) {
        return delegate.setOwner(uuid);
    }

    @Override
    public boolean canDeposit(UUID uuid, Number number, Currency currency) {
        return delegate.canDeposit(uuid, number, delegate(currency));
    }

    @Override
    public boolean canWithdraw(UUID uuid, Number number, Currency currency) {
        return delegate.canWithdraw(uuid, number, delegate(currency));
    }
}
