import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// Abstract Subscription class
abstract class Subscription {
    String id, customer, phone, nextDate, recurring, plan, status;

    public Subscription(String id, String customer, String phone, String nextDate, String recurring, String plan, String status) {
        this.id = id;
        this.customer = customer;
        this.phone = phone;
        this.nextDate = nextDate;
        this.recurring = recurring;
        this.plan = plan;
        this.status = status;
    }

    public abstract String getType();

    public Object[] toRow() {
        return new Object[]{id, customer, phone, nextDate, recurring, plan, status, getType()};
    }
}

// ProductSubscription class
class ProductSubscription extends Subscription {
    public ProductSubscription(String id, String customer, String phone, String nextDate, String recurring, String plan, String status) {
        super(id, customer, phone, nextDate, recurring, plan, status);
    }

    @Override
    public String getType() {
        return "Product";
    }
}

// ServiceSubscription class
class ServiceSubscription extends Subscription {
    public ServiceSubscription(String id, String customer, String phone, String nextDate, String recurring, String plan, String status) {
        super(id, customer, phone, nextDate, recurring, plan, status);
    }

    @Override
    public String getType() {
        return "Service";
    }
}

// Subscription Manager
class SubscriptionManager {
    private List<Subscription> subscriptions = new ArrayList<>();

    public SubscriptionManager(List<Subscription> initialData) {
        subscriptions.addAll(initialData);
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(String customer, String phone, String nextDate, String recurring, String plan, String status, String type) {
        String id = generateNewId();
        Subscription sub = type.equalsIgnoreCase("Product") ?
                new ProductSubscription(id, customer, phone, nextDate, recurring, plan, status) :
                new ServiceSubscription(id, customer, phone, nextDate, recurring, plan, status);
        subscriptions.add(sub);
    }

    public String generateNewId() {
        return "S" + String.format("%04d", subscriptions.size() + 1);
    }

    public Subscription findById(String id) {
        return subscriptions.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
    }

    public void editSubscription(String id, String customer, String phone, String nextDate, String recurring, String plan, String status, String type) {
        Subscription old = findById(id);
        if (old != null) {
            subscriptions.remove(old);
            Subscription updated = type.equalsIgnoreCase("Product") ?
                    new ProductSubscription(id, customer, phone, nextDate, recurring, plan, status) :
                    new ServiceSubscription(id, customer, phone, nextDate, recurring, plan, status);
            subscriptions.add(updated);
        }
    }

    public void removeSubscription(String id) {
        subscriptions.removeIf(s -> s.id.equals(id));
    }

    public List<Subscription> filter(String keyword) {
        keyword = keyword.toLowerCase();
        List<Subscription> result = new ArrayList<>();
        for (Subscription s : subscriptions) {
            if (s.id.toLowerCase().contains(keyword) ||
                s.customer.toLowerCase().contains(keyword) ||
                s.phone.contains(keyword) ||
                s.plan.toLowerCase().contains(keyword) ||
                s.status.toLowerCase().contains(keyword)) {
                result.add(s);
            }
        }
        return result;
    }
}

// GUI class
public class SubscriptionManagerGUI extends JFrame {
    private SubscriptionManager manager;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public SubscriptionManagerGUI() {
        List<Subscription> initialSubs = List.of(
                new ProductSubscription("S0001", "Sophie Tness", "99119911", "09/25/2025", "$35.00", "Monthly", "In Progress"),
                new ServiceSubscription("S0002", "Dan Durance", "99112233", "09/25/2025", "$35.00", "Monthly", "In Progress")
        );
        manager = new SubscriptionManager(initialSubs);

        setTitle(" Subscription Manager");
        setSize(900, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Customer", "Phone", "Next Date", "Recurring", "Plan", "Status", "Type"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons and Search
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton removeBtn = new JButton("Remove");
        searchField = new JTextField(15);

        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(removeBtn);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        // Actions
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                Subscription sub = manager.findById(id);
                showForm(sub);
            }
        });
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                manager.removeSubscription(id);
                refreshTable();
            }
        });

        // Live search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });

        refreshTable();
    }

    private void showForm(Subscription existing) {
        JTextField customer = new JTextField(existing != null ? existing.customer : "");
        JTextField phone = new JTextField(existing != null ? existing.phone : "");
        JTextField nextDate = new JTextField(existing != null ? existing.nextDate : "");
        JTextField recurring = new JTextField(existing != null ? existing.recurring : "");
        JComboBox<String> planBox = new JComboBox<>(new String[]{"Monthly", "Yearly"});
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"In Progress", "Quotation", "Closed"});
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Product", "Service"});

        if (existing != null) {
            planBox.setSelectedItem(existing.plan);
            statusBox.setSelectedItem(existing.status);
            typeBox.setSelectedItem(existing.getType());
        }

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Customer:")); panel.add(customer);
        panel.add(new JLabel("Phone:")); panel.add(phone);
        panel.add(new JLabel("Next Date:")); panel.add(nextDate);
        panel.add(new JLabel("Recurring:")); panel.add(recurring);
        panel.add(new JLabel("Plan:")); panel.add(planBox);
        panel.add(new JLabel("Status:")); panel.add(statusBox);
        panel.add(new JLabel("Type:")); panel.add(typeBox);

        int result = JOptionPane.showConfirmDialog(this, panel, existing == null ? "Add Subscription" : "Edit Subscription", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (existing == null) {
                manager.addSubscription(customer.getText(), phone.getText(), nextDate.getText(), recurring.getText(),
                        planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
            } else {
                manager.editSubscription(existing.id, customer.getText(), phone.getText(), nextDate.getText(), recurring.getText(),
                        planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
            }
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText();
        List<Subscription> list = keyword.isEmpty() ? manager.getSubscriptions() : manager.filter(keyword);
        for (Subscription s : list) {
            tableModel.addRow(s.toRow());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SubscriptionManagerGUI().setVisible(true));
    }
}
