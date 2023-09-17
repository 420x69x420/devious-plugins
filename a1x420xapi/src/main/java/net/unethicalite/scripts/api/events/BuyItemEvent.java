package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.item.ItemPrice;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Prices;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.OwnedItems;

import java.util.List;

@Slf4j
public class BuyItemEvent extends AbstractEvent {
    private String name;
    private int id;
    private int amount;
    private int price;
    private boolean useROW = true;
    private boolean startAmountSet;
    private int startAmount;

    public BuyItemEvent() {
        this.setEventTimeoutTicks(200);
    }

    public String getName() {
        if (name == null && id != 0) {
            name = Static.getItemManager().getItemComposition(id).getName();
        }
        return name;
    }

    public BuyItemEvent setName(String name) {
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

    public BuyItemEvent setId(int id) {
        this.id = id;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public BuyItemEvent setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public BuyItemEvent setPrice(int price) {
        this.price = price;
        return this;
    }

    public boolean isUseROW() {
        return useROW;
    }

    public BuyItemEvent setUseROW(boolean useROW) {
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

        if (!startAmountSet) {
            startAmount = OwnedItems.getCount(true, getName());
            log.info("[BUY ITEM EVENT] - Setting start amount to " + startAmount + " with goal amount " + startAmount + getAmount());
            startAmountSet = true;
        }

        if (OwnedItems.getCount(true, getName()) >= (startAmount + getAmount())) {
            this.setEventCompleted(true);
            return;
        }

        BankLocation bankLocation = BankLocation.GRAND_EXCHANGE_BANK;
        if (!bankLocation.getArea().contains(Players.getLocal())) {
            if (isUseROW() && !Inventory.contains(x -> x.getName().contains("Ring of wealth("))) {
                if (!Bank.isOpen()) {
                    log.info("[BUY ITEM EVENT] - Opening bank to withdraw ring of wealth");
                    new OpenBankEvent()
                            .execute();
                    return;
                }

                if (Inventory.isFull()) {
                    log.info("[BUY ITEM EVENT] - Depositing inventory to withdraw ring of wealth");
                    Bank.depositInventory();
                    Time.sleepTicksUntil(() -> !Inventory.isFull(), 3);
                    return;
                }

                if (Bank.contains(x -> x.getName().contains("Ring of wealth"))) {
                    log.info("[BUY ITEM EVENT] - Withdrawing ring of wealth");
                    Bank.withdraw(x -> x.getName().contains("Ring of wealth"), 1, Bank.WithdrawMode.ITEM);
                    Time.sleepTicksUntil(() -> Inventory.contains(x -> x.getName().contains("Ring of wealth")), 3);
                    return;
                }

                log.info("[BUY ITEM EVENT] - Can't find ring of wealth, so walking it is");
                setUseROW(false);
                return;
            }

            log.info("[BUY ITEM EVENT] - Walking to Grand Exchange");
            new MovementEvent()
                    .setDestination(bankLocation)
                    .execute();
            return;
        }

        if (Inventory.isFull()) {
            log.info("[BUY ITEM EVENT] - Depositing inventory to make space");
            new BankInventoryEvent()
                    .execute();
            return;
        }

        if (!GrandExchange.isOpen()) {
            log.info("[BUY ITEM EVENT] - Opening grand exchange");
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

        log.info("[BUY ITEM EVENT] - Buying " + getAmount() + " " + getName() + " (ID: " + getId() + ") for " + price + " each");
        GrandExchange.buy(getId(), getAmount(), priceToBuy(), false, false);
    }

    private int priceToBuy() {
        int itemPrice = (getPrice() != 0) ? getPrice() : Prices.getItemPrice(getId());
        int totalPrice = itemPrice * getAmount();
        if (itemPrice != 0) {
            return itemPrice;
        }
        return (getAmount() == 1)
                ? (itemPrice < 1000) ? itemPrice * 5 : (itemPrice < 1_000_000) ? (int) (itemPrice * 1.2) : (int) (itemPrice * 1.1)
                : (totalPrice < 500_000) ? itemPrice * 2 : (totalPrice < 1_000_000) ? (int) (itemPrice * 1.5) : (int) (itemPrice * 1.2);
    }
}
