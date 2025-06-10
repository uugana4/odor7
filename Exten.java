package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

enum Role { ADMIN, USER }
enum OrderStatus { PAID, PENDING, CANCELLED }

class Product {
    private static int nextId = 1;
    private int id;
    private String name;
    private String category;
    private double price;
    private String code;
    private int stock;

    public Product(String name, String category, double price, String code, int stock) {
        this.id = nextId++;
        this.name = name;
        this.category = category;
        this.price = price;
        this.code = code;
        this.stock = stock;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getCode() { return code; }
    public int getStock() { return stock; }
    public void setPrice(double price) { this.price = price; }
    public void addStock(int qty) { this.stock += qty; }
    public void reduceStock(int qty) { this.stock -= qty; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
}

class User {
    private String username;
    private String password;
    private Role role;
    private double balance = 0;
    private List<Order> orders = new ArrayList<>();

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public boolean checkPassword(String pw) { return password.equals(pw); }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public double getBalance() { return balance; }
    public void addBalance(double amount) { balance += amount; }
    public boolean deductBalance(double amount) {
        if (balance >= amount) { balance -= amount; return true; }
        return false;
    }
    public void addOrder(Order o) { orders.add(o); }
    public List<Order> getOrders() { return orders; }
}

class Order {
    private static int nextOrderId = 1;
    private int orderId;
    private List<Product> products = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private double total;
    private OrderStatus status;
    private String paymentId;
    private LocalDateTime date;

    public Order() {
        this.orderId = nextOrderId++;
        this.status = OrderStatus.PENDING;
        this.date = LocalDateTime.now();
    }
    public void addProduct(Product p, int qty) {
        products.add(p);
        quantities.add(qty);
        total += p.getPrice() * qty;
    }
    public int getOrderId() { return orderId; }
    public double getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus s) { status = s; }
    public void setPaymentId(String pid) { paymentId = pid; }
    public String getPaymentId() { return paymentId; }
    public LocalDateTime getDate() { return date; }
    public List<Product> getProducts() { return products; }
    public List<Integer> getQuantities() { return quantities; }

    // Файлд хадгалах/уншихад зориулсан нэмэлт конструктор
    public Order(int orderId, double total, OrderStatus status, String paymentId, LocalDateTime date) {
        this.orderId = orderId;
        this.total = total;
        this.status = status;
        this.paymentId = paymentId;
        this.date = date;
    }
}

public class Exten {
    static final Logger logger = LogManager.getLogger(Exten.class);
    static List<User> users = new ArrayList<>();
    static List<Product> products = new ArrayList<>();
    static Map<String, Double> coupons = new HashMap<>();

    // --- Бараа нэмэх/нэгтгэх ---
    public static void addOrMergeProduct(String name, String category, String code, double price, int stock) {
        try {
            if (name == null || name.trim().isEmpty())
                throw new IllegalArgumentException("Барааны нэр хоосон байж болохгүй.");
            if (category == null || category.trim().isEmpty())
                throw new IllegalArgumentException("Ангилал хоосон байж болохгүй.");
            if (code == null || code.trim().isEmpty())
                throw new IllegalArgumentException("Код хоосон байж болохгүй.");
            if (price < 0)
                throw new IllegalArgumentException("Үнэ сөрөг байж болохгүй.");
            if (stock < 0)
                throw new IllegalArgumentException("Үлдэгдэл сөрөг байж болохгүй.");
            for (Product p : products)
                if (p.getCode().equalsIgnoreCase(code))
                    throw new IllegalArgumentException("Ийм кодтой бараа бүртгэлтэй байна!");
            products.add(new Product(name, category, price, code, stock));
            logger.info("Бараа нэмэгдсэн: {}", name);
        } catch (IllegalArgumentException e) {
            logger.error("Бараа нэмэхэд алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Бараа устгах ---
    public static void deleteProduct(int id) {
        try {
            Product toRemove = null;
            for (Product p : products) if (p.getId() == id) toRemove = p;
            if (toRemove == null) throw new IllegalArgumentException("Бараа олдсонгүй.");
            products.remove(toRemove);
            logger.info("Бараа устгагдсан: id={}", id);
        } catch (IllegalArgumentException e) {
            logger.error("Бараа устгахад алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Хэрэглэгч бүртгэх ---
    public static void signUp(String username, String password, String role) {
        try {
            for (User u : users)
                if (u.getUsername().equals(username))
                    throw new IllegalArgumentException("Ийм нэртэй хэрэглэгч байна!");
            users.add(new User(username, password, "admin".equals(role) ? Role.ADMIN : Role.USER));
            logger.info("Шинэ хэрэглэгч бүртгэгдсэн: {}", username);
        } catch (IllegalArgumentException e) {
            logger.error("Хэрэглэгч бүртгэхэд алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Мөнгө нэмэх ---
    public static void addBalance(User user, double amt) {
        try {
            if (amt < 0) throw new IllegalArgumentException("Сөрөг мөнгө нэмэх боломжгүй!");
            user.addBalance(amt);
            logger.info("Хэрэглэгч {} дансандаа {}₮ нэмсэн.", user.getUsername(), amt);
        } catch (IllegalArgumentException e) {
            logger.error("Мөнгө нэмэхэд алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Купон нэмэх ---
    public static void addCoupon(String code, double percent) {
        try {
            if (code == null || code.trim().isEmpty())
                throw new IllegalArgumentException("Купон код хоосон байж болохгүй.");
            if (coupons.containsKey(code))
                throw new IllegalArgumentException("Код бүртгэлтэй байна!");
            if (percent <= 0 || percent > 100)
                throw new IllegalArgumentException("Хөнгөлөлтийн хувь 0-100 хооронд байх ёстой.");
            coupons.put(code, percent);
            logger.info("Купон нэмэгдсэн: {}, хувь: {}", code, percent);
        } catch (IllegalArgumentException e) {
            logger.error("Купон нэмэхэд алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Захиалга хийх ---
    public static Order makeOrder(User user, List<Integer> productIds, List<Integer> quantities, String couponCode) {
        try {
            if (productIds.size() != quantities.size())
                throw new IllegalArgumentException("Барааны ID болон тоо ширхэгийн хэмжээ таарахгүй байна.");
            Order order = new Order();
            for (int i = 0; i < productIds.size(); i++) {
                Product p = null;
                for (Product pr : products) if (pr.getId() == productIds.get(i)) p = pr;
                if (p == null) throw new IllegalArgumentException("Бараа олдсонгүй.");
                int qty = quantities.get(i);
                if (qty <= 0) throw new IllegalArgumentException("Тоо ширхэг 0-ээс их байх ёстой.");
                if (qty > p.getStock()) throw new IllegalArgumentException("Үлдэгдэл хүрэлцэхгүй.");
                order.addProduct(p, qty);
                p.reduceStock(qty);
            }
            if (order.getTotal() == 0) throw new IllegalArgumentException("Захиалга хоосон.");
            double discount = 0;
            if (couponCode != null && !couponCode.isEmpty()) {
                if (!coupons.containsKey(couponCode))
                    throw new IllegalArgumentException("Купон код буруу.");
                discount = coupons.get(couponCode);
            }
            double pay = order.getTotal() * (1 - discount / 100);
            if (user.getBalance() < pay) {
                order.setStatus(OrderStatus.CANCELLED);
                user.addOrder(order);
                logger.warn("Захиалга цуцлагдлаа. Үлдэгдэл хүрэлцэхгүй: {}", user.getUsername());
                throw new IllegalArgumentException("Үлдэгдэл хүрэлцэхгүй. Захиалга цуцлагдлаа.");
            } else {
                user.deductBalance(pay);
                order.setStatus(OrderStatus.PAID);
                order.setPaymentId("PAY" + order.getOrderId());
                user.addOrder(order);
                logger.info("Захиалга амжилттай: {} -> orderId={}", user.getUsername(), order.getOrderId());
            }
            return order;
        } catch (IllegalArgumentException e) {
            logger.error("Захиалга хийхэд алдаа гарлаа: {}", e.getMessage());
            throw e;
        }
    }

    // --- Файл хадгалах/унших (алдаа гарвал exception) ---
    public static void saveProductsToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Product p : products) {
                pw.printf("%d,%s,%s,%.2f,%s,%d\n",
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getCode(), p.getStock());
            }
            logger.info("Бараанууд файлуудад хадгалагдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Бараа хадгалах үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Бараа хадгалах үед алдаа гарлаа: " + e.getMessage());
        }
    }
    public static void loadProductsFromFile(String filename) {
        products.clear();
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String[] arr = fileScanner.nextLine().split(",");
                if (arr.length == 6) {
                    Product p = new Product(
                        arr[1],
                        arr[2],
                        Double.parseDouble(arr[3]),
                        arr[4],
                        Integer.parseInt(arr[5])
                    );
                    products.add(p);
                }
            }
            logger.info("Бараанууд файлуудаас уншигдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Бараа унших үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Бараа унших үед алдаа гарлаа: " + e.getMessage());
        }
    }

    public static void saveUsersToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (User u : users) {
                pw.printf("%s,%s,%s,%.2f\n",
                    u.getUsername(), u.getPassword(), u.getRole(), u.getBalance());
            }
            logger.info("Хэрэглэгчид файлуудад хадгалагдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Хэрэглэгч хадгалах үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Хэрэглэгч хадгалах үед алдаа гарлаа: " + e.getMessage());
        }
    }
    public static void loadUsersFromFile(String filename) {
        users.clear();
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String[] arr = fileScanner.nextLine().split(",");
                if (arr.length == 4) {
                    User u = new User(
                        arr[0],
                        arr[1],
                        arr[2].equals("ADMIN") ? Role.ADMIN : Role.USER
                    );
                    u.addBalance(Double.parseDouble(arr[3]));
                    users.add(u);
                }
            }
            logger.info("Хэрэглэгчид файлуудаас уншигдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Хэрэглэгч унших үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Хэрэглэгч унших үед алдаа гарлаа: " + e.getMessage());
        }
    }

    public static void saveOrdersToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (User u : users) {
                for (Order o : u.getOrders()) {
                    pw.printf("%s,%d,%.2f,%s,%s,%s\n",
                        u.getUsername(),
                        o.getOrderId(),
                        o.getTotal(),
                        o.getStatus(),
                        o.getPaymentId() == null ? "" : o.getPaymentId(),
                        o.getDate().toString()
                    );
                    for (int i = 0; i < o.getProducts().size(); i++) {
                        Product p = o.getProducts().get(i);
                        int qty = o.getQuantities().get(i);
                        pw.printf("ITEM,%d,%d\n", p.getId(), qty);
                    }
                }
            }
            logger.info("Захиалгууд файлуудад хадгалагдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Захиалга хадгалах үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Захиалга хадгалах үед алдаа гарлаа: " + e.getMessage());
        }
    }
    public static void loadOrdersFromFile(String filename) {
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            User currentUser = null;
            Order currentOrder = null;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] arr = line.split(",");
                if (arr.length >= 6 && !arr[0].equals("ITEM")) {
                    String username = arr[0];
                    int orderId = Integer.parseInt(arr[1]);
                    double total = Double.parseDouble(arr[2]);
                    OrderStatus status = OrderStatus.valueOf(arr[3]);
                    String paymentId = arr[4].isEmpty() ? null : arr[4];
                    LocalDateTime date = LocalDateTime.parse(arr[5]);
                    for (User u : users) {
                        if (u.getUsername().equals(username)) {
                            currentUser = u;
                            currentOrder = new Order(orderId, total, status, paymentId, date);
                            u.addOrder(currentOrder);
                            break;
                        }
                    }
                } else if (arr.length == 3 && arr[0].equals("ITEM") && currentOrder != null) {
                    int productId = Integer.parseInt(arr[1]);
                    int qty = Integer.parseInt(arr[2]);
                    for (Product p : products) {
                        if (p.getId() == productId) {
                            currentOrder.addProduct(p, qty);
                            break;
                        }
                    }
                }
            }
            logger.info("Захиалгууд файлуудаас уншигдлаа: {}", filename);
        } catch (Exception e) {
            logger.error("Захиалга унших үед алдаа гарлаа: {}", e.getMessage());
            throw new RuntimeException("Захиалга унших үед алдаа гарлаа: " + e.getMessage());
        }
    }
}