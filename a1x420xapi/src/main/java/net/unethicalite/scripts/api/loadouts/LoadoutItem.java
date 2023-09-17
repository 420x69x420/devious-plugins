package net.unethicalite.scripts.api.loadouts;

import net.runelite.http.api.item.ItemPrice;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.OwnedItems;

import java.util.List;

public class LoadoutItem {
    private String name;
    private int id;
    private int amount;
    private int minAmount;
    private int maxAmount;
    private int price;
    private int restock;
    private boolean noted;
    private boolean strict;
    private boolean equipment;

    public LoadoutItem() {

    }

    public LoadoutItem(int id, int amount, int price, int restock, boolean noted, boolean strict, boolean equipment) {
        this.id = id;
        this.amount = amount;
        this.price = price;
        this.restock = restock;
        this.noted = noted;
        this.strict = strict;
        this.equipment = equipment;
    }

    public LoadoutItem(int id, int minAmount, int maxAmount, int price, int restock, boolean noted, boolean strict, boolean equipment) {
        this.id = id;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.price = price;
        this.restock = restock;
        this.noted = noted;
        this.strict = strict;
        this.equipment = equipment;
    }

    public LoadoutItem(String name, int amount, int price, int restock, boolean noted, boolean strict, boolean equipment) {
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.restock = restock;
        this.noted = noted;
        this.strict = strict;
        this.equipment = equipment;
    }

    public LoadoutItem(String name, int minAmount, int maxAmount, int price, int restock, boolean noted, boolean strict, boolean equipment) {
        this.name = name;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.price = price;
        this.restock = restock;
        this.noted = noted;
        this.strict = strict;
        this.equipment = equipment;
    }

    public LoadoutItem setName(String name) {
        this.name = name;
        return this;
    }

    public LoadoutItem setId(int id) {
        this.id = id;
        return this;
    }

    public LoadoutItem setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public LoadoutItem setMinAmount(int minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public LoadoutItem setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    public LoadoutItem setPrice(int price) {
        this.price = price;
        return this;
    }

    public LoadoutItem setRestock(int restock) {
        this.restock = restock;
        return this;
    }

    public LoadoutItem setNoted(boolean noted) {
        this.noted = noted;
        return this;
    }

    public LoadoutItem setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public LoadoutItem setEquipment(boolean equipment) {
        this.equipment = equipment;
        return this;
    }

    public String getName() {
        if (name == null && id != 0) {
            name = Static.getItemManager().getItemComposition(id).getName();
        }
        return name;
    }

    public int getId() {
        if (id == 0 && name != null) {
            List<ItemPrice> itemPriceList = Static.getItemManager().search(name);
            for (ItemPrice itemPrice : itemPriceList) {
                if (itemPrice.getName().equals(name)) {
                    id = itemPrice.getId();
                    break;
                }
            }
        }
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getPrice() {
        return price;
    }

    public int getRestock() {
        return restock;
    }

    public boolean isNoted() {
        return noted;
    }

    public boolean isStrict() {
        return strict;
    }

    public boolean isEquipment() {
        return equipment;
    }

    public boolean hasItem() {
        int targetAmount = getMinAmount() != 0 ? (Bank.isOpen() ? getMaxAmount() : getMinAmount()) : getAmount();

        if (isEquipment()) {
            int equipmentAmount = Equipment.getCount(true, x -> x.getName().equals(getName()));
            return isStrict() ? (getMaxAmount() != 0 ? (equipmentAmount >= targetAmount && equipmentAmount <= getMaxAmount()) : equipmentAmount == targetAmount) : (equipmentAmount >= targetAmount);
        } else {
            int inventoryAmount = Inventory.getCount(true, x -> x.getName().equals(getName()) && ((isNoted() && x.isNoted()) || (!isNoted() && !x.isNoted())));
            return isStrict() ? (getMaxAmount() != 0 ? (inventoryAmount >= targetAmount && inventoryAmount <= getMaxAmount()) : inventoryAmount == targetAmount) : (inventoryAmount >= targetAmount);
        }
    }


    public boolean ownsItem() {
        int ownedAmount = OwnedItems.getCount(true, getName());
        int targetAmount = getMaxAmount() != 0 ? getMaxAmount() : getAmount();

        return ownedAmount >= targetAmount;
    }
}
