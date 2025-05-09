import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PayrollSystemGUI extends JFrame {
    private ArrayList<Employee> employees;
    private JTabbedPane tabbedPane;
    private JTable employeeTable;
    private DecimalFormat pesoFormat = new DecimalFormat("₱###,###.##");
    private Font customFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Color primaryColor = new Color(0, 102, 204);
    private Color secondaryColor = new Color(240, 240, 240);
    private Color accentColor = new Color(255, 153, 0);

    public PayrollSystemGUI() {
        super("Philippine Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        initializeDatabase();
        initUI();
        refreshEmployeeList();
    }

    private void initializeDatabase() {
        DatabaseHelper.initializeDatabase();
        try {
            employees = Employee.loadAll();
        } catch (SQLException e) {
            showError("Error loading employees: " + e.getMessage());
            employees = new ArrayList<>();
        }
    }

    private void initUI() {
        UIManager.put("TabbedPane.background", secondaryColor);
        UIManager.put("TabbedPane.selected", primaryColor);
        UIManager.put("Button.background", primaryColor);
        UIManager.put("Button.foreground", Color.BLUE);
        UIManager.put("Button.font", customFont.deriveFont(Font.BOLD));
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 255), 
                    getWidth(), getHeight(), Color.WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("PHILIPPINE PAYROLL SYSTEM (HOURLY + WEEKLY + MONTHLY)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        employeeTable = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return c;
            }
        };
        employeeTable.setFont(customFont);
        employeeTable.setRowHeight(30);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Employee", createAddEmployeePanel());
        tabbedPane.addTab("View/Edit", createViewEditPanel());
        tabbedPane.addTab("Payroll", createPayrollPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createAddEmployeePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField nameField = new JTextField(20);
        JTextField positionField = new JTextField(20);
        JTextField hourlyRateField = new JTextField(20);
        JTextField workingDaysField = new JTextField(20);
        JLabel statusLabel = new JLabel(" ");
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(createLabel("Add New Employee", 18), gbc);
        
        gbc.gridwidth = 1; gbc.gridy++;
        formPanel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createLabel("Position:"), gbc);
        gbc.gridx = 1;
        formPanel.add(positionField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createLabel("Hourly Rate:"), gbc);
        gbc.gridx = 1;
        formPanel.add(hourlyRateField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createLabel("Working Days per Week (1-7):"), gbc);
        gbc.gridx = 1;
        formPanel.add(workingDaysField, gbc);
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = createStyledButton("Save Employee", primaryColor);
        formPanel.add(saveButton, gbc);
        
        gbc.gridy++;
        formPanel.add(statusLabel, gbc);
        
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String position = positionField.getText().trim();
                double hourlyRate = Double.parseDouble(hourlyRateField.getText());
                int workingDays = Integer.parseInt(workingDaysField.getText());

                if (name.isEmpty() || position.isEmpty()) {
                    statusLabel.setText("Name and position cannot be empty!");
                    return;
                }

                if (workingDays < 1 || workingDays > 7) {
                    statusLabel.setText("Working days must be between 1 and 7");
                    return;
                }

                Employee emp = new Employee(name, position, hourlyRate, workingDays);
                emp.save();
                employees.add(emp);
                refreshEmployeeList();
                
                statusLabel.setText("Employee added successfully! ID: " + emp.getId());
                nameField.setText("");
                positionField.setText("");
                hourlyRateField.setText("");
                workingDaysField.setText("");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
        
        panel.add(formPanel);
        return panel;
    }

    private JPanel createViewEditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(createTitledBorder("Employee Records"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(createStyledButton("Refresh", primaryColor, e -> refreshEmployeeList()));
        buttonPanel.add(createStyledButton("Edit Hours", accentColor, e -> editEmployeeHours()));
        buttonPanel.add(createStyledButton("Edit Overtime", accentColor, e -> editEmployeeOvertime()));
        buttonPanel.add(createStyledButton("Edit Working Days", accentColor, e -> editWorkingDays()));
        buttonPanel.add(createStyledButton("Delete", new Color(204, 0, 0), e -> deleteSelectedEmployee()));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JTabbedPane payrollTabs = new JTabbedPane();
        payrollTabs.addTab("Daily Payroll", createDailyPayrollPanel());
        payrollTabs.addTab("Weekly Payroll", createWeeklyPayrollPanel());
        payrollTabs.addTab("Monthly Payroll", createMonthlyPayrollPanel());
        
        panel.add(payrollTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDailyPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JTextArea payrollArea = new JTextArea();
        payrollArea.setFont(customFont);
        payrollArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(payrollArea);
        scrollPane.setBorder(createTitledBorder("Daily Payroll Results"));
        
        JButton processButton = createStyledButton("Calculate Daily Payroll", primaryColor, e -> {
            payrollArea.setText("===== DAILY PAYROLL REPORT =====\n\n");
            double totalGross = 0, totalDeductions = 0, totalNet = 0;
            
            for (Employee emp : employees) {
                payrollArea.append(String.format("%-20s (ID: %d)\n", emp.getName(), emp.getId()));
                payrollArea.append(String.format("  Position: %s\n", emp.getPosition()));
                payrollArea.append(String.format("  Hourly Rate: %s\n", pesoFormat.format(emp.getHourlyRate())));
                payrollArea.append(String.format("  Regular Hours: %.2f\n", emp.getHoursWorked()));
                payrollArea.append(String.format("  Overtime Hours: %.2f\n", emp.getOvertimeHours()));
                payrollArea.append(String.format("  Daily Gross Pay: %s\n", pesoFormat.format(emp.calculateDailyGrossPay())));
                payrollArea.append("  Daily Deductions:\n");
                payrollArea.append(String.format("    SSS: %s\n", pesoFormat.format(emp.getSss()/(emp.getWorkingDays()*4))));
                payrollArea.append(String.format("    PhilHealth: %s\n", pesoFormat.format(emp.getPhilhealth()/(emp.getWorkingDays()*4))));
                payrollArea.append(String.format("    Pag-IBIG: %s\n", pesoFormat.format(emp.getPagibig()/(emp.getWorkingDays()*4))));
                payrollArea.append(String.format("    Tax: %s\n", pesoFormat.format(emp.getTax()/(emp.getWorkingDays()*4))));
                payrollArea.append(String.format("  DAILY NET PAY: %s\n\n", pesoFormat.format(emp.calculateDailyNetPay())));
                
                totalGross += emp.calculateDailyGrossPay();
                totalDeductions += (emp.getSss()/(emp.getWorkingDays()*4) + emp.getPhilhealth()/(emp.getWorkingDays()*4) + 
                                  emp.getPagibig()/(emp.getWorkingDays()*4) + emp.getTax()/(emp.getWorkingDays()*4));
                totalNet += emp.calculateDailyNetPay();
            }
            
            payrollArea.append("\n======================\n");
            payrollArea.append(String.format("TOTAL DAILY GROSS PAYROLL: %s\n", pesoFormat.format(totalGross)));
            payrollArea.append(String.format("TOTAL DAILY DEDUCTIONS: %s\n", pesoFormat.format(totalDeductions)));
            payrollArea.append(String.format("TOTAL DAILY NET PAYROLL: %s", pesoFormat.format(totalNet)));
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(processButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createWeeklyPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JTextArea payrollArea = new JTextArea();
        payrollArea.setFont(customFont);
        payrollArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(payrollArea);
        scrollPane.setBorder(createTitledBorder("Weekly Payroll Results"));
        
        JButton processButton = createStyledButton("Calculate Weekly Payroll", primaryColor, e -> {
            payrollArea.setText("===== WEEKLY PAYROLL REPORT =====\n\n");
            double totalGross = 0, totalDeductions = 0, totalNet = 0;
            
            for (Employee emp : employees) {
                payrollArea.append(String.format("%-20s (ID: %d)\n", emp.getName(), emp.getId()));
                payrollArea.append(String.format("  Position: %s\n", emp.getPosition()));
                payrollArea.append(String.format("  Hourly Rate: %s\n", pesoFormat.format(emp.getHourlyRate())));
                payrollArea.append(String.format("  Regular Hours: %.2f/day\n", emp.getHoursWorked()));
                payrollArea.append(String.format("  Overtime Hours: %.2f/week\n", emp.getOvertimeHours()*emp.getWorkingDays()));
                payrollArea.append(String.format("  Working Days: %d days\n", emp.getWorkingDays()));
                payrollArea.append(String.format("  Weekly Gross Pay: %s\n", pesoFormat.format(emp.calculateWeeklyGrossPay())));
                payrollArea.append("  Weekly Deductions:\n");
                payrollArea.append(String.format("    SSS: %s\n", pesoFormat.format(emp.getSss()/4)));
                payrollArea.append(String.format("    PhilHealth: %s\n", pesoFormat.format(emp.getPhilhealth()/4)));
                payrollArea.append(String.format("    Pag-IBIG: %s\n", pesoFormat.format(emp.getPagibig()/4)));
                payrollArea.append(String.format("    Tax: %s\n", pesoFormat.format(emp.getTax()/4)));
                payrollArea.append(String.format("  WEEKLY NET PAY: %s\n\n", pesoFormat.format(emp.calculateWeeklyNetPay())));
                
                totalGross += emp.calculateWeeklyGrossPay();
                totalDeductions += (emp.getSss()/4 + emp.getPhilhealth()/4 + emp.getPagibig()/4 + emp.getTax()/4);
                totalNet += emp.calculateWeeklyNetPay();
            }
            
            payrollArea.append("\n======================\n");
            payrollArea.append(String.format("TOTAL WEEKLY GROSS PAYROLL: %s\n", pesoFormat.format(totalGross)));
            payrollArea.append(String.format("TOTAL WEEKLY DEDUCTIONS: %s\n", pesoFormat.format(totalDeductions)));
            payrollArea.append(String.format("TOTAL WEEKLY NET PAYROLL: %s", pesoFormat.format(totalNet)));
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(processButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMonthlyPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JTextArea payrollArea = new JTextArea();
        payrollArea.setFont(customFont);
        payrollArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(payrollArea);
        scrollPane.setBorder(createTitledBorder("Monthly Payroll Results"));
        
        JButton processButton = createStyledButton("Calculate Monthly Payroll", primaryColor, e -> {
            payrollArea.setText("===== MONTHLY PAYROLL REPORT =====\n\n");
            double totalGross = 0, totalDeductions = 0, totalNet = 0;
            
            for (Employee emp : employees) {
                payrollArea.append(String.format("%-20s (ID: %d)\n", emp.getName(), emp.getId()));
                payrollArea.append(String.format("  Position: %s\n", emp.getPosition()));
                payrollArea.append(String.format("  Hourly Rate: %s\n", pesoFormat.format(emp.getHourlyRate())));
                payrollArea.append(String.format("  Regular Hours: %.2f/day\n", emp.getHoursWorked()));
                payrollArea.append(String.format("  Overtime Hours: %.2f/month\n", emp.getOvertimeHours()*emp.getWorkingDays()*4));
                payrollArea.append(String.format("  Working Days: %d days/week\n", emp.getWorkingDays()));
                payrollArea.append(String.format("  Monthly Gross Pay: %s\n", pesoFormat.format(emp.calculateMonthlyGrossPay())));
                payrollArea.append("  Monthly Deductions:\n");
                payrollArea.append(String.format("    SSS: %s\n", pesoFormat.format(emp.getSss())));
                payrollArea.append(String.format("    PhilHealth: %s\n", pesoFormat.format(emp.getPhilhealth())));
                payrollArea.append(String.format("    Pag-IBIG: %s\n", pesoFormat.format(emp.getPagibig())));
                payrollArea.append(String.format("    Tax: %s\n", pesoFormat.format(emp.getTax())));
                payrollArea.append(String.format("  MONTHLY NET PAY: %s\n\n", pesoFormat.format(emp.calculateMonthlyNetPay())));
                
                totalGross += emp.calculateMonthlyGrossPay();
                totalDeductions += (emp.getSss() + emp.getPhilhealth() + emp.getPagibig() + emp.getTax());
                totalNet += emp.calculateMonthlyNetPay();
            }
            
            payrollArea.append("\n======================\n");
            payrollArea.append(String.format("TOTAL MONTHLY GROSS PAYROLL: %s\n", pesoFormat.format(totalGross)));
            payrollArea.append(String.format("TOTAL MONTHLY DEDUCTIONS: %s\n", pesoFormat.format(totalDeductions)));
            payrollArea.append(String.format("TOTAL MONTHLY NET PAYROLL: %s", pesoFormat.format(totalNet)));
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(processButton, BorderLayout.SOUTH);
        return panel;
    }

    private void editWorkingDays() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee");
            return;
        }

        int id = (int) employeeTable.getValueAt(selectedRow, 0);
        String name = (String) employeeTable.getValueAt(selectedRow, 1);

        while (true) {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter new working days per week (1-7) for " + name + ":", 
                "Edit Working Days", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // User canceled

            try {
                int days = Integer.parseInt(input);
                if (days < 1 || days > 7) {
                    showError("Working days must be between 1 and 7");
                    continue;
                }

                for (Employee emp : employees) {
                    if (emp.getId() == id) {
                        emp.setWorkingDays(days);
                        emp.save();
                        refreshEmployeeList();
                        showMessage("Working days updated successfully");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                return;
            }
        }
    }

    private void refreshEmployeeList() {
        try {
            employees = Employee.loadAll();
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Name", "Position", "Hourly Rate", "Regular Hours", "Overtime", "Working Days"}, 0
            ) {
                public boolean isCellEditable(int row, int column) { return false; }
            };

            for (Employee emp : employees) {
                model.addRow(new Object[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPosition(),
                    pesoFormat.format(emp.getHourlyRate()),
                    emp.getHoursWorked(),
                    emp.getOvertimeHours(),
                    emp.getWorkingDays()
                });
            }

            employeeTable.setModel(model);
        } catch (SQLException e) {
            showError("Error loading employees: " + e.getMessage());
        }
    }

    private void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee to delete");
            return;
        }

        int id = (int) employeeTable.getValueAt(selectedRow, 0);
        try {
            for (Employee emp : employees) {
                if (emp.getId() == id) {
                    if (JOptionPane.showConfirmDialog(this, 
                        "Delete employee " + emp.getName() + "?", 
                        "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        emp.delete();
                        employees.remove(emp);
                        refreshEmployeeList();
                        showMessage("Employee deleted successfully");
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            showError("Error deleting employee: " + e.getMessage());
        }
    }

    private void editEmployeeHours() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee");
            return;
        }

        int id = (int) employeeTable.getValueAt(selectedRow, 0);
        String name = (String) employeeTable.getValueAt(selectedRow, 1);

        while (true) {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter new regular hours (0-12) for " + name + ":", 
                "Edit Regular Hours", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // User canceled

            try {
                double hours = Double.parseDouble(input);
                if (hours < 0 || hours > 12) {
                    showError("Regular hours must be between 0 and 12");
                    continue;
                }

                for (Employee emp : employees) {
                    if (emp.getId() == id) {
                        emp.setHoursWorked(hours);
                        emp.save();
                        refreshEmployeeList();
                        showMessage("Regular hours updated successfully");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                return;
            }
        }
    }

    private void editEmployeeOvertime() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee");
            return;
        }

        int id = (int) employeeTable.getValueAt(selectedRow, 0);
        String name = (String) employeeTable.getValueAt(selectedRow, 1);

        while (true) {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter new overtime hours for " + name + ":", 
                "Edit Overtime Hours", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return; // User canceled

            try {
                double hours = Double.parseDouble(input);
                if (hours < 0) {
                    showError("Overtime hours cannot be negative");
                    continue;
                }

                for (Employee emp : employees) {
                    if (emp.getId() == id) {
                        emp.setOvertimeHours(hours);
                        emp.save();
                        refreshEmployeeList();
                        showMessage("Overtime hours updated successfully");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                return;
            }
        }
    }

    private JLabel createLabel(String text) {
        return createLabel(text, 14);
    }
    
    private JLabel createLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(customFont.deriveFont(Font.BOLD, size));
        label.setForeground(primaryColor);
        return label;
    }
    
    private Border createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            customFont.deriveFont(Font.BOLD),
            primaryColor
        );
    }
    
    private JButton createStyledButton(String text, Color bgColor, ActionListener action) {
        JButton button = createStyledButton(text, bgColor);
        button.addActionListener(action);
        return button;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD));
        button.setBackground(bgColor);
        button.setForeground(Color.BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.brighter()); }
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        return button;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new PayrollSystemGUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}