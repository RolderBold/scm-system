package com.scm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.scm.dao.InventoryDAO;
import com.scm.dao.OrderDAO;
import com.scm.models.User;

public class DashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private User currentUser;
    private JPanel contentPanel;
    private JLabel welcomeLabel;
    private JLabel inventoryStatusLabel;
    private JLabel reorderItemsLabel;
    private JLabel pendingOrdersLabel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("SCM System - Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel navPanel = createNavigationPanel();
        mainPanel.add(navPanel, BorderLayout.WEST);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        showDashboardContent();
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(44, 62, 80)); // #2C3E50
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        navPanel.setPreferredSize(new Dimension(200, getHeight()));

        Font buttonFont = new Font("Roboto Mono", Font.BOLD, 14);

        JButton dashboardBtn = createNavButton("DASHBOARD", buttonFont);
        JButton inventoryBtn = createNavButton("INVENTORY", buttonFont);
        JButton suppliersBtn = createNavButton("SUPPLIERS", buttonFont);
        JButton ordersBtn = createNavButton("ORDERS", buttonFont);
        JButton logoutBtn = createNavButton("LOGOUT", buttonFont);

        dashboardBtn.addActionListener(e -> showDashboardContent());
        inventoryBtn.addActionListener(e -> openInventoryFrame());
        suppliersBtn.addActionListener(e -> openSuppliersFrame());
        ordersBtn.addActionListener(e -> openOrdersFrame());
        logoutBtn.addActionListener(e -> logout());

        navPanel.add(dashboardBtn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(inventoryBtn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(suppliersBtn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(ordersBtn);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(Box.createVerticalGlue());
        navPanel.add(logoutBtn);

        return navPanel;
    }

    private JButton createNavButton(String text, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setForeground(new Color(236, 240, 241)); // #ECF0F1
        button.setBackground(new Color(52, 73, 94)); // #34495E
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(236, 240, 241)); // #ECF0F1
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(new Font("Roboto Mono", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(44, 62, 80)); // #2C3E50
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private void showDashboardContent() {
        contentPanel.removeAll();
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel dashboardTitle = new JLabel("DASHBOARD");
        dashboardTitle.setFont(new Font("Roboto Mono", Font.BOLD, 24));
        dashboardTitle.setForeground(new Color(44, 62, 80)); // #2C3E50
        dashboardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        RoundedPanel inventoryCard = new RoundedPanel(20);
        inventoryCard.setBackground(new Color(66, 134, 244));
        inventoryCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel inventoryTitle = new JLabel("Inventory Status");
        inventoryTitle.setFont(new Font("Roboto Mono", Font.BOLD, 16));
        inventoryTitle.setForeground(Color.WHITE);
        inventoryCard.add(inventoryTitle, gbc);
        gbc.gridy = 1;
        inventoryStatusLabel = new JLabel("Loading...");
        inventoryStatusLabel.setFont(new Font("Roboto Mono", Font.PLAIN, 14));
        inventoryStatusLabel.setForeground(Color.WHITE);
        inventoryCard.add(inventoryStatusLabel, gbc);
        summaryPanel.add(inventoryCard);

        RoundedPanel reorderCard = new RoundedPanel(20);
        reorderCard.setBackground(new Color(76, 175, 80));
        reorderCard.setLayout(new GridBagLayout());
        gbc.gridy = 0;
        JLabel reorderTitle = new JLabel("Items to Reorder");
        reorderTitle.setFont(new Font("Roboto Mono", Font.BOLD, 16));
        reorderTitle.setForeground(Color.WHITE);
        reorderCard.add(reorderTitle, gbc);
        gbc.gridy = 1;
        reorderItemsLabel = new JLabel("Loading...");
        reorderItemsLabel.setFont(new Font("Roboto Mono", Font.PLAIN, 14));
        reorderItemsLabel.setForeground(Color.WHITE);
        reorderCard.add(reorderItemsLabel, gbc);
        summaryPanel.add(reorderCard);

        RoundedPanel ordersCard = new RoundedPanel(20);
        ordersCard.setBackground(new Color(255, 152, 0));
        ordersCard.setLayout(new GridBagLayout());
        gbc.gridy = 0;
        JLabel ordersTitle = new JLabel("Pending Orders");
        ordersTitle.setFont(new Font("Roboto Mono", Font.BOLD, 16));
        ordersTitle.setForeground(Color.WHITE);
        ordersCard.add(ordersTitle, gbc);
        gbc.gridy = 1;
        pendingOrdersLabel = new JLabel("Loading...");
        pendingOrdersLabel.setFont(new Font("Roboto Mono", Font.PLAIN, 14));
        pendingOrdersLabel.setForeground(Color.WHITE);
        ordersCard.add(pendingOrdersLabel, gbc);
        summaryPanel.add(ordersCard);

        JPanel activitiesPanel = new JPanel(new BorderLayout());
        activitiesPanel.setBackground(Color.WHITE);
        activitiesPanel.setBorder(BorderFactory.createTitledBorder("Recent Activities"));
        activitiesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea activitiesArea = new JTextArea();
        activitiesArea.setFont(new Font("Roboto Mono", Font.PLAIN, 12));
        activitiesArea.setEditable(false);
        activitiesArea.setText("System initialized\n* Database connected\n* User logged in: " + currentUser.getUsername());
        JScrollPane activitiesScroll = new JScrollPane(activitiesArea);
        activitiesPanel.add(activitiesScroll, BorderLayout.CENTER);

        dashboardPanel.add(dashboardTitle);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(summaryPanel);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(activitiesPanel);

        contentPanel.add(dashboardPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        updateSummaryData();
    }

    private void updateSummaryData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int totalProducts;
            private int reorderItems;
            private int pendingOrders;

            @Override
            protected Void doInBackground() throws Exception {
                InventoryDAO inventoryDAO = new InventoryDAO();
                totalProducts = inventoryDAO.getTotalProducts();
                reorderItems = inventoryDAO.getReorderItemsCount();
                OrderDAO orderDAO = new OrderDAO();
                pendingOrders = orderDAO.getPendingOrdersCount();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    inventoryStatusLabel.setText(totalProducts + " products");
                    reorderItemsLabel.setText(reorderItems + " items");
                    pendingOrdersLabel.setText(pendingOrders + " orders");
                } catch (Exception e) {
                    System.err.println("Error updating summary data: " + e.getMessage());
                    inventoryStatusLabel.setText("Error");
                    reorderItemsLabel.setText("Error");
                    pendingOrdersLabel.setText("Error");
                }
            }
        };
        worker.execute();
    }

    private void openInventoryFrame() {
        InventoryFrame inventoryFrame = new InventoryFrame();
        inventoryFrame.setVisible(true);
    }

    private void openSuppliersFrame() {
        SuppliersFrame suppliersFrame = new SuppliersFrame();
        suppliersFrame.setVisible(true);
    }

    private void openOrdersFrame() {
        OrdersFrame ordersFrame = new OrdersFrame();
        ordersFrame.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    // Custom RoundedPanel class for cards
    class RoundedPanel extends JPanel {
        private int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }
}