package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ShoppingItemModel {

    private String shoppingItemUid;
    private String name;
    private boolean inBasket;

    public ShoppingItemModel() {
        shoppingItemUid = null;
        name = null;
        inBasket = false;
    }

    public ShoppingItemModel(String shoppingItemUid, String name, boolean inBasket) {
        this.shoppingItemUid = shoppingItemUid;
        this.name = name;
        this.inBasket = inBasket;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("shoppingItemUid", shoppingItemUid);
        map.put("name", name);
        map.put("inBasket", inBasket);
        return map;
    }

    public void fromMap(Map<String, Object> map) {
        String _shoppingItemUid = (String) map.get("shoppingItemUid");
        if (_shoppingItemUid != null) {
            shoppingItemUid = _shoppingItemUid;
        }

        String _name = (String) map.get("name");
        if (_name != null) {
            name = _name;
        }

        Object _inBasket = map.get("inBasket");
        if (_inBasket instanceof Boolean) {
            inBasket = (Boolean) _inBasket;
        }
    }

    public String getShoppingItemUid() {
        return shoppingItemUid;
    }

    public void setShoppingItemUid(String shoppingItemUid) {
        this.shoppingItemUid = shoppingItemUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInBasket() {
        return inBasket;
    }

    public void setInBasket(boolean inBasket) {
        this.inBasket = inBasket;
    }

    @Override
    public int hashCode() {
        return shoppingItemUid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ShoppingItemModel s && s.shoppingItemUid != null &&
                s.shoppingItemUid.equals(shoppingItemUid);
    }

    @NonNull
    @Override
    public String toString() {
        return "ShoppingItemModel{" +
                "shoppingItemUid='" + shoppingItemUid + '\'' +
                ", name='" + name + '\'' +
                ", inBasket=" + inBasket +
                '}';
    }
}
