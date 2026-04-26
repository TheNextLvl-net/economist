import org.jspecify.annotations.NullMarked;

@NullMarked
module net.thenextlvl.economist {
    exports net.thenextlvl.economist.bank;
    exports net.thenextlvl.economist.currency;
    exports net.thenextlvl.economist;

    requires net.kyori.adventure;
    requires net.thenextlvl.binder;
    requires org.bukkit;

    requires static org.jetbrains.annotations;
    requires static org.jspecify;
}