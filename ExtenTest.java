package com.example;
import java.util.*;

public class ExtenTest {
    public static void main(String[] args) {
        // 1. Хэрэглэгч бүртгэх
        Exten.signUp("testuser", "password", "user");
        User user = Exten.users.get(0);

        // 2. Мөнгө нэмэх
        Exten.addBalance(user, 100000);

        // 3. Бараа нэмэх
        Exten.addOrMergeProduct("Notebook", "Stationery", "NB001", 5000, 20);
        Exten.addOrMergeProduct("Pen", "Stationery", "PEN01", 1000, 50);

        // 4. Купон нэмэх
        Exten.addCoupon("SALE10", 10);

        // 5. Захиалга хийх (амжилттай)
        List<Integer> productIds = Arrays.asList(Exten.products.get(0).getId(), Exten.products.get(1).getId());
        List<Integer> quantities = Arrays.asList(2, 5);
        Exten.makeOrder(user, productIds, quantities, "SALE10");

        // 6. Бараа устгах
        Exten.deleteProduct(Exten.products.get(1).getId());

        // 7. Алдаатай бараа нэмэх (нэр хоосон)
        try {
            Exten.addOrMergeProduct("", "Stationery", "NB002", 4000, 10);
        } catch (Exception e) {
            // Алдаа гарах ёстой
        }

        // 8. Алдаатай захиалга (үлдэгдэл хүрэлцэхгүй)
        try {
            Exten.makeOrder(user, Arrays.asList(Exten.products.get(0).getId()), Arrays.asList(1000), null);
        } catch (Exception e) {
            // Алдаа гарах ёстой
        }

        // 9. Алдаатай купон
        try {
            Exten.makeOrder(user, Arrays.asList(Exten.products.get(0).getId()), Arrays.asList(1), "BADCODE");
        } catch (Exception e) {
            // Алдаа гарах ёстой
        }

    }
}