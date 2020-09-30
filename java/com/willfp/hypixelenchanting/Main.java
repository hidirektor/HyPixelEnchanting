package com.willfp.hypixelenchanting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
    public static Main instance;
    ItemStack NOENCHANTMENT;
    HashMap<Player, HypixelEnchantment> playerHypixelEnchantmentMap;

    public Main() {
        this.NOENCHANTMENT = new ItemStack(Material.GRAY_DYE, 1);
        this.playerHypixelEnchantmentMap = new HashMap();
    }

    public static int scheduleSyncDelayedTask(Runnable runnable, int delay) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, (long)delay);
    }

    public void onLoad() {
        instance = this;
    }

    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(this, this);
        ItemMeta meta = this.NOENCHANTMENT.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("noenchant")));
        this.NOENCHANTMENT.setItemMeta(meta);
        this.saveDefaultConfig();
    }

    public void onDisable() {
        instance = null;
    }

    public static Main getInstance() {
        return instance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if (cmd.getName().equalsIgnoreCase("updateitemlore")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("onlyplayers")));
            } else {
                player = (Player)sender;
                if (player.hasPermission("hypixelenchanting.update")) {
                    player.setItemInHand(HypixelEnchantment.addEnchantmentLore(player.getItemInHand()));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("nopermission")));
                }
            }

            return true;
        } else if (cmd.getName().equalsIgnoreCase("hereload")) {
            if (!(sender instanceof Player)) {
                this.reloadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("reloadedconfig")));
            } else {
                player = (Player)sender;
                if (player.hasPermission("hypixelenchanting.reload")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("reloadedconfig")));
                    this.reloadConfig();
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("nopermission")));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void function(final InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        if (player.getOpenInventory().getTopInventory() != null) {
            if (player.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")))) {
                if (event.getClickedInventory() != null) {
                    if (event.getCursor() != null || event.getClickedInventory().getType() != InventoryType.PLAYER) {
                        if (event.getCurrentItem() != null) {
                            if (event.getSlot() == 49 && event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")))) {
                                player.closeInventory();
                            }

                            if (event.getSlot() != 13 && event.getSlot() != 29 && event.getSlot() != 31 && event.getSlot() != 33 && event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")))) {
                                event.setCancelled(true);
                            }

                            final HypixelEnchantment hypixelEnchantment = (HypixelEnchantment)this.playerHypixelEnchantmentMap.get(player);
                            if (event.getSlot() != 29 && event.getSlot() != 31 && event.getSlot() != 33) {
                                if (event.getClickedInventory().getType() == InventoryType.PLAYER && event.getCursor() == null) {
                                    return;
                                }

                                if (event.getClickedInventory().getType() == InventoryType.PLAYER && player.getInventory().getItem(event.getSlot()) == null) {
                                    return;
                                }

                                Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
                                    public void run() {
                                        if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("inventory-name"))) || event.getSlot() == 13) {
                                            hypixelEnchantment.setItem(player);
                                        }
                                    }
                                }, 1L);
                            } else if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")))) {
                                hypixelEnchantment.addEnchantment(player, event);
                            }

                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void combineXPBook(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (event.getCursor().getType() == Material.ENCHANTED_BOOK) {
                    if (player.getInventory().getItem(event.getSlot()) != null) {
                        if (HypixelEnchantment.enchantabilityMap.containsKey(new ItemStack(player.getInventory().getItem(event.getSlot()).getType(), 1))) {
                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)event.getCursor().getItemMeta();
                            Map<Enchantment, Integer> enchants = meta.getStoredEnchants();
                            ArrayList<Enchantment> appliedEnchants = new ArrayList();
                            ItemMeta meta2 = player.getInventory().getItem(event.getSlot()).getItemMeta();
                            Iterator var7 = enchants.keySet().iterator();

                            while(true) {
                                Enchantment e;
                                while(true) {
                                    do {
                                        label61:
                                        while(true) {
                                            do {
                                                if (!var7.hasNext()) {
                                                    if (appliedEnchants.isEmpty()) {
                                                        return;
                                                    }

                                                    player.getInventory().setItem(event.getSlot(), HypixelEnchantment.addEnchantmentLore(player.getInventory().getItem(event.getSlot())));
                                                    event.setCancelled(true);
                                                    player.setItemOnCursor((ItemStack)null);
                                                    return;
                                                }

                                                e = (Enchantment)var7.next();
                                            } while(!e.canEnchantItem(player.getInventory().getItem(event.getSlot())));

                                            if (meta2.hasEnchant(e)) {
                                                break;
                                            }

                                            Iterator var9 = meta2.getEnchants().keySet().iterator();

                                            while(var9.hasNext()) {
                                                Enchantment e2 = (Enchantment)var9.next();
                                                if (e.conflictsWith(e2)) {
                                                    continue label61;
                                                }
                                            }

                                            player.getInventory().getItem(event.getSlot()).addUnsafeEnchantment(e, (Integer)enchants.get(e));
                                            appliedEnchants.add(e);
                                        }
                                    } while((Integer)enchants.get(e) < meta2.getEnchantLevel(e));

                                    if (meta.getEnchantLevel(e) == (Integer)enchants.get(e)) {
                                        player.getInventory().getItem(event.getSlot()).addEnchantment(e, (Integer)enchants.get(e));
                                        break;
                                    }

                                    if ((Integer)enchants.get(e) + 1 <= e.getMaxLevel()) {
                                        player.getInventory().getItem(event.getSlot()).addEnchantment(e, (Integer)enchants.get(e) + 1);
                                        break;
                                    }
                                }

                                appliedEnchants.add(e);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEnchantmentClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getClickedBlock().getType().equals(Material.ENCHANTING_TABLE)) {
                    event.setCancelled(true);
                    ItemStack YELLOW_DISPLAY_PANE = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1, (short)4);
                    ItemMeta meta = YELLOW_DISPLAY_PANE.getItemMeta();
                    meta.setDisplayName("§r");
                    YELLOW_DISPLAY_PANE.setItemMeta(meta);
                    ItemStack BLACK_DISPLAY_PANE = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, (short)15);
                    meta = BLACK_DISPLAY_PANE.getItemMeta();
                    meta.setDisplayName("§r");
                    BLACK_DISPLAY_PANE.setItemMeta(meta);
                    ItemStack RED_DISPLAY_PANE = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1, (short)14);
                    meta = RED_DISPLAY_PANE.getItemMeta();
                    meta.setDisplayName("§r");
                    RED_DISPLAY_PANE.setItemMeta(meta);
                    ItemStack BARRIER_DISPLAY = new ItemStack(Material.BARRIER, 1);
                    meta = BARRIER_DISPLAY.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("exit")));
                    BARRIER_DISPLAY.setItemMeta(meta);
                    Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")));
                    ItemStack[] items = new ItemStack[]{YELLOW_DISPLAY_PANE, BLACK_DISPLAY_PANE, RED_DISPLAY_PANE, RED_DISPLAY_PANE, RED_DISPLAY_PANE, RED_DISPLAY_PANE, RED_DISPLAY_PANE, BLACK_DISPLAY_PANE, YELLOW_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, RED_DISPLAY_PANE, new ItemStack(Material.AIR, 1), RED_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, this.NOENCHANTMENT, BLACK_DISPLAY_PANE, this.NOENCHANTMENT, BLACK_DISPLAY_PANE, this.NOENCHANTMENT, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, YELLOW_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BARRIER_DISPLAY, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, BLACK_DISPLAY_PANE, YELLOW_DISPLAY_PANE};
                    inv.setContents(items);
                    player.setVelocity(new Vector(0, 0, 0));
                    player.openInventory(inv);
                    this.playerHypixelEnchantmentMap.put(player, new HypixelEnchantment());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player)event.getPlayer();
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("inventory-name")))) {
            return;
        } else {
            if (event.getInventory().getItem(13) == null) {
                return;
            } else {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), event.getInventory().getItem(13));
                } else {
                    player.getInventory().addItem(new ItemStack[]{event.getInventory().getItem(13)});
                }
            }
            this.playerHypixelEnchantmentMap.remove(player);
        }

    }

    public static double randInt(double min, double max) {
        double x = (double)((int)(Math.random() * (max - min + 1.0D))) + min;
        return x;
    }

    public static double randFloat(double min, double max) {
        double x = Math.random() * (max - min + 1.0D) + min;
        return x;
    }

    public static String getNumeral(int Int) {
        LinkedHashMap<String, Integer> roman_numerals = new LinkedHashMap();
        roman_numerals.put("M", 1000);
        roman_numerals.put("CM", 900);
        roman_numerals.put("D", 500);
        roman_numerals.put("CD", 400);
        roman_numerals.put("C", 100);
        roman_numerals.put("XC", 90);
        roman_numerals.put("L", 50);
        roman_numerals.put("XL", 40);
        roman_numerals.put("X", 10);
        roman_numerals.put("IX", 9);
        roman_numerals.put("V", 5);
        roman_numerals.put("IV", 4);
        roman_numerals.put("I", 1);
        String res = "";

        Entry entry;
        for(Iterator var3 = roman_numerals.entrySet().iterator(); var3.hasNext(); Int %= (Integer)entry.getValue()) {
            entry = (Entry)var3.next();
            int matches = Int / (Integer)entry.getValue();
            res = res + repeat((String)entry.getKey(), matches);
        }

        return res;
    }

    public static String repeat(String s, int n) {
        if (s == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < n; ++i) {
                sb.append(s);
            }

            return sb.toString();
        }
    }
}