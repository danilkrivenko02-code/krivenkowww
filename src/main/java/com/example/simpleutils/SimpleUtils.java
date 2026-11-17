package com.example.simpleutils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SimpleUtils extends JavaPlugin {

    /** Кто кого ждёт: target -> инициатор запроса tpa */
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("SimpleUtils запущен");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков");
            return true;
        }
        switch (command.getName().toLowerCase()) {
            case "setspawn" -> cmdSetSpawn(player);
            case "spawn" -> cmdSpawn(player);
            case "sethome" -> cmdSetHome(player);
            case "home" -> cmdHome(player);
            case "tpa" -> cmdTpa(player, args);
            case "tpaccept" -> cmdTpAccept(player);
        }
        return true;
    }

    private void cmdSetSpawn(Player p) {
        getConfig().set("spawn", p.getLocation());
        saveConfig();
        p.sendMessage(msg("Точка спавна установлена", NamedTextColor.GREEN));
    }

    private void cmdSpawn(Player p) {
        Location spawn = getConfig().getLocation("spawn");
        if (spawn == null) {
            p.sendMessage(msg("Спавн не установлен", NamedTextColor.RED));
            return;
        }
        p.teleport(spawn);
        p.sendMessage(msg("Телепорт на спавн", NamedTextColor.GREEN));
    }

    private void cmdSetHome(Player p) {
        String path = "homes." + p.getUniqueId();
        getConfig().set(path, p.getLocation());
        saveConfig();
        p.sendMessage(msg("Дом установлен", NamedTextColor.GREEN));
    }

    private void cmdHome(Player p) {
        Location home = getConfig().getLocation("homes." + p.getUniqueId());
        if (home == null) {
            p.sendMessage(msg("Дом не установлен. Используй /sethome", NamedTextColor.RED));
            return;
        }
        p.teleport(home);
        p.sendMessage(msg("Добро пожаловать домой", NamedTextColor.GREEN));
    }

    private void cmdTpa(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(msg("Используй: /tpa <игрок>", NamedTextColor.RED));
            return;
        }
        OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
        if (target == null || !target.isOnline()) {
            p.sendMessage(msg("Игрок не найден или не в сети", NamedTextColor.RED));
            return;
        }
        Player online = target.getPlayer();
        if (online.equals(p)) {
            p.sendMessage(msg("Нельзя телепортироваться к себе", NamedTextColor.RED));
            return;
        }
        tpaRequests.put(online.getUniqueId(), p.getUniqueId());
        online.sendMessage(msg(p.getName() + " хочет телепортироваться к тебе. /tpaccept — принять", NamedTextColor.YELLOW));
        p.sendMessage(msg("Запрос отправлен", NamedTextColor.GREEN));
    }

    private void cmdTpAccept(Player p) {
        UUID requesterId = tpaRequests.remove(p.getUniqueId());
        if (requesterId == null) {
            p.sendMessage(msg("Нет входящих запросов", NamedTextColor.RED));
            return;
        }
        OfflinePlayer requester = getServer().getOfflinePlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            p.sendMessage(msg("Игрок уже не в сети", NamedTextColor.RED));
            return;
        }
        requester.getPlayer().teleport(p.getLocation());
        p.sendMessage(msg(requester.getName() + " телепортировался к тебе", NamedTextColor.GREEN));
    }

    private Component msg(String text, NamedTextColor color) {
        return Component.text(text, color);
    }
}
