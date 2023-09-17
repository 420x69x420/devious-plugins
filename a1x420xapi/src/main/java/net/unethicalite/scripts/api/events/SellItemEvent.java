package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.item.ItemPrice;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.extended.ExBank;
import net.unethicalite.scripts.api.extended.ExInvy;

import java.util.List;

@Slf4j
public class SellItemEvent extends AbstractEvent {
    private String name;
    private int id;
    private int amount;
    private int price;
    private boolean useROW = true;
    private boolean startAmountSet;
    private int startAmount;

    public SellItemEvent() {
        this.setEventTimeoutTicks(200);
        this.price = 1;
    }

    public String getName() {
        if (name == null && id != 0) {
            name = Static.getItemManager().getItemComposition(id).getName();
        }
        return name;
    }

    public SellItemEvent setName(String name) {
        this.name = name;
        return this;
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

    public SellItemEvent setId(int id) {
        this.id = id;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public SellItemEvent setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public SellItemEvent setPrice(int price) {
        this.price = price;
        return this;
    }

    public boolean isUseROW() {
        return useROW;
    }

    public SellItemEvent setUseROW(boolean useROW) {
        this.useROW = useROW;
        return this;
    }

    public int getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(int startAmount) {
        this.startAmount = startAmount;
        startAmountSet = true;
    }

    @Override
    public void onLoop() {
        if (GrandExchange.isOpen()) {
            if (!GrandExchange.isEmpty()) {
                if (GrandExchange.canCollect()) {
                    log.info("[SELL ITEM EVENT] - Collecting offers");
                    GrandExchange.collect(false);
                    return;
                }
                log.info("[SELL ITEM EVENT] - Waiting for GE offers to complete");
                return;
            }
        }
        if (!startAmountSet) {
            startAmount = OwnedItems.getCount(true, getName());
            log.info("[SELL ITEM EVENT] - Setting start amount to " + startAmount + " with goal amount " + (startAmount - getAmount()));
            startAmountSet = true;
        }

        if (OwnedItems.getCount(true, getName()) <= (startAmount - getAmount())) {
            this.setEventCompleted(true);
            return;
        }

        BankLocation bankLocation = BankLocation.GRAND_EXCHANGE_BANK;
        if (!bankLocation.getArea().contains(Players.getLocal())) {
            if (isUseROW() && !Inventory.contains(x -> x.getName().contains("Ring of wealth("))) {
                if (!Bank.isOpen()) {
                    log.info("[SELL ITEM EVENT] - Opening bank to withdraw ring of wealth");
                    new OpenBankEvent()
                            .execute();
                    return;
                }

                if (Inventory.isFull()) {
                    log.info("[SELL ITEM EVENT] - Depositing inventory to withdraw ring of wealth");
                    Bank.depositInventory();
                    Time.sleepTicksUntil(() -> !Inventory.isFull(), 3);
                    return;
                }

                if (Bank.contains(x -> x.getName().contains("Ring of wealth"))) {
                    log.info("[SELL ITEM EVENT] - Withdrawing ring of wealth");
                    Bank.withdraw(x -> x.getName().contains("Ring of wealth"), 1, Bank.WithdrawMode.ITEM);
                    Time.sleepTicksUntil(() -> Inventory.contains(x -> x.getName().contains("Ring of wealth")), 3);
                    return;
                }

                log.info("[SELL ITEM EVENT] - Can't find ring of wealth, so walking it is");
                setUseROW(false);
                return;
            }

            log.info("[SELL ITEM EVENT] - Walking to Grand Exchange");
            new MovementEvent()
                    .setDestination(bankLocation)
                    .execute();
            return;
        }
        //find all version of item (nonstackable + notable, or stackable)
        boolean stackable = Static.getItemManager().getItemComposition(getId()).isStackable();
        int itemID;
        if (stackable) {
            itemID = getId();
        } else {
            itemID = OwnedItems.getNotedID(getId());
        }
        if (Bank.isOpen()) {
            if (Bank.Inventory.getCount(true, itemID) >= getAmount()) {
                Bank.close();
                return;
            }

            if (!ExInvy.containsOnly(itemID)) {
                log.info("[SELL ITEM EVENT] - Depositing inventory to make space");
                ExBank.depositAllExcept(itemID);
                return;
            }
            if (Equipment.contains(getId())) {
                Bank.depositEquipment();
                return;
            }
            Bank.withdraw(getId(), getAmount(), Bank.WithdrawMode.NOTED);
            return;
        }

        if (Inventory.getCount(true, itemID) < getAmount()) {
            log.info("[SELL ITEM EVENT] - Opening bank for withdrawal of our sell items");
            new OpenBankEvent().execute();
            return;
        }

        if (!GrandExchange.isOpen()) {
            log.info("[SELL ITEM EVENT] - Opening grand exchange");
            new OpenGEEvent()
                    .execute();
            return;
        }

        if (!GrandExchange.isEmpty()) {
            if (GrandExchange.canCollect()) {
                log.info("[BUY ITEM EVENT] - Collecting completed offer");
                GrandExchange.collect(false);
                Time.sleepTicksUntil(() -> !GrandExchange.canCollect(), 3);
                return;
            }

            log.info("[BUY ITEM EVENT] - Waiting for offer to complete");
            return;
        }

        log.info("[SELL ITEM EVENT] - Selling " + getAmount() + " " + getName() + " (ID: " + itemID + ") for " + price + " each");
        GrandExchange.sell(itemID, getAmount(), getPrice(), false, false);
    }

}
