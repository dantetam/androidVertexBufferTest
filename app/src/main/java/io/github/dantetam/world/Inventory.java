package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Dante on 8/11/2016.
 */
public class Inventory {

    private List<Item> inventory;

    public Inventory() {
        inventory = new ArrayList<>();
    }

    public void addAnotherInventory(Inventory other) {
        for (Item item: other.getInventory()) {
            addToInventory(item);
        }
    }
    public void addAllToInventory(Collection<Item> items) {
        for (Item item: items) {
            addToInventory(item);
        }
    }
    public void addAllToInventory(Item[] items) {
        for (Item item: items) {
            addToInventory(item);
        }
    }
    public void addToInventory(Item addItem) {
        changeInventory(addItem, true);
    }
    public void subtractFromInventory(Item addItem) {
        changeInventory(addItem, false);
    }
    private void changeInventory(Item addItem, boolean positive) {
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (addItem.type == item.type) {
                if (positive) {
                    item.quantity += addItem.quantity;
                }
                else {
                    item.quantity -= addItem.quantity;
                }
                if (item.quantity == 0) {
                    inventory.remove(i);
                }
                return;
            }
        }
        inventory.add(addItem);
    }
    public List<Item> getInventory() {
        return inventory;
    }

    public boolean hasItemsInInventory(Item[] items, boolean remove) {
        List<Item> inventoryItems = new ArrayList<>();
        for (Item item: items) {
            boolean reqFound = false;
            for (Item inventoryItem: inventory) {
                if (item.equals(inventoryItem) && item.quantity > inventoryItem.quantity) {
                    if (remove) {
                        inventoryItems.add(inventoryItem);
                    }
                    reqFound = true;
                    break;
                }
            }
            if (!reqFound) {
                return false;
            }
        }
        if (remove) {
            for (Item requested: items) {
                //inventoryItems.get(i).quantity -= requested.quantity;
                subtractFromInventory(requested);
            }
        }
        return true;
    }

}
