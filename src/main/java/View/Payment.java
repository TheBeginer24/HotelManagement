/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;

/**
 *
 * @author ASUS
 */

import com.toedter.calendar.JDateChooser;
import Control.*;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.*;
import java.util.List;

public class Payment extends JFrame {

    private JComboBox<Customer> customerCBX;
    private DefaultComboBoxModel<Customer> customerNameModel;
    private CustomerControl customerControl = new CustomerControl(); 

    private JDateChooser checkInField;
    private JDateChooser checkOutField;
    private JTextField totalService;
    private JTextField totalRoom;
    private JComboBox<String> statusComboBox;
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;
    private NumberFormat currencyFormat;
    private JButton saveBtn, printBtn, exitBtn;
    private int idRoom,idBill = 0;
    private boolean isClick = false;
    private Room slRoom;
    private BillControl bc = new BillControl();

    public Payment(int id) {
        this.idRoom = id;
        this.slRoom = new RoomControl().getbyID(id);
        currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setTitle("Đặt phòng");
        if(slRoom.getStatus() == 1 && bc.getRoomBill(slRoom.getNum()) != null)
        {
            billData(bc.getRoomBill(slRoom.getNum()));
        }
    }

    private void initializeComponents() {
        // Khởi tạo các text field
        customerNameModel = new DefaultComboBoxModel<>();
        customerCBX = new JComboBox<>(customerNameModel);
        customerCBX.setEditable(true);

        // Load dữ liệu từ CustomerControl
        List<Customer> customers = customerControl.getAll(); // hoặc getTop10() nếu bạn có
        int limit = 0;
        for (Customer c : customers) {
            customerNameModel.addElement(c);
        }

        checkOutField = new JDateChooser();
        checkInField = new JDateChooser();

        checkInField.setDateFormatString("dd/MM/yyyy");
        checkOutField.setDateFormatString("dd/MM/yyyy");
        totalRoom = new JTextField("0");
        // Khởi tạo ComboBox cho trạng thái
        totalService = new JTextField("0");
        String[] statusOptions = { "Chưa thanh toán","Hoàn tất"};
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setSelectedItem("Chưa hoàn tất");

        // Khởi tạo bảng
        String[] columnNames = {"TÊN SP - DV", "SL", "ĐƠN GIÁ", "THÀNH TIỀN"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Chỉ cho phép chỉnh sửa cột SL
            }
        };

        serviceTable = new JTable(tableModel);
        serviceTable.setRowHeight(25);
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Styling cho header
        JTableHeader header = serviceTable.getTableHeader();
        header.setBackground(new Color(220, 220, 220));
        header.setFont(new Font("Arial", Font.BOLD, 12));

        totalAmountLabel = new JLabel();
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalAmountLabel.setForeground(Color.RED);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(new Color(240, 240, 240));

        saveBtn = new JButton("💾 Lưu");
        printBtn = new JButton("🖨️ In");
        exitBtn = new JButton("❌ Thoát");

        toolbarPanel.add(saveBtn);
        toolbarPanel.add(printBtn);
        toolbarPanel.add(exitBtn);

        // Top panel container
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(toolbarPanel, BorderLayout.SOUTH);

        // Panel thông tin khách hàng
        JPanel customerInfoPanel = createCustomerInfoPanel();

        // Panel danh sách sản phẩm
        JPanel servicePanel = createServicePanel();

        // Sắp xếp layout
        mainPanel.add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(customerInfoPanel, BorderLayout.NORTH);
        centerPanel.add(servicePanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void billData(Bill b)
    {
        idBill = b.getId();
        Customer csInfo = customerControl.getById(b.getUser());
        customerCBX.setSelectedItem(csInfo);
        checkInField.setDate(b.getCheck_in());
        checkOutField.setDate(b.getCheck_out());
        totalService.setText(formatPrice(b.getTotal_service()));
        totalRoom.setText(formatPrice(b.getTotal_time()));
        totalAmountLabel.setText(formatPrice(b.getTotal()));
        setTblData(tableModel, new billDetailControl().getByBill(b.getId()));
    }
    
    private void setTblData(DefaultTableModel model,List<BillDetail> list)
    {
        List<Service> svl = new ArrayList<>();
        svl = new ServiceControl().getAll();
        for (int i=0;i<list.size();i++)
        {
            String svName = "";
            int j=0;
            while(list.get(i).getService() != svl.get(j).getId())
            {
                j++;
            }
            svName = svl.get(j).getName();
            model.addRow(new Object[]{svName,list.get(i).getQuant(),formatPrice(list.get(i).getTotal()/list.get(i).getQuant()),formatPrice(list.get(i).getTotal())});
        }
        model.fireTableDataChanged();
    }
    
   private JPanel createCustomerInfoPanel() {
    // Panel chứa toàn bộ (trái + phải)
    JPanel mainPanel = new JPanel(new BorderLayout());

    // ==================== PANEL TRÁI ====================
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new GridBagLayout());
    leftPanel.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;

    // Row 0 - Khách hàng
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
    leftPanel.add(new JLabel("Khách hàng:"), gbc);

    JPanel customerInputPanel = new JPanel(new BorderLayout());
    customerInputPanel.add(customerCBX, BorderLayout.CENTER);
    JButton btnOpenCustomer = new JButton("...");
    btnOpenCustomer.setPreferredSize(new Dimension(40, 25));
    btnOpenCustomer.addActionListener(e -> {
        new CustomerList().setVisible(true);
    });
    customerInputPanel.add(btnOpenCustomer, BorderLayout.EAST);

    gbc.gridx = 1; gbc.gridwidth = 2;
    leftPanel.add(customerInputPanel, gbc);

    // Row 0 - Trạng thái
    gbc.gridx = 3; gbc.gridwidth = 1;
    leftPanel.add(new JLabel("Trạng thái:"), gbc);

    gbc.gridx = 4;
    leftPanel.add(statusComboBox, gbc);

    // Row 1 - Ngày đặt
    gbc.gridx = 0; gbc.gridy = 1;
    leftPanel.add(new JLabel("Ngày đặt:"), gbc);
    gbc.gridx = 1; gbc.gridwidth = 2;
    leftPanel.add(checkInField, gbc);

    // Row 1 - Tiền dịch vụ
    gbc.gridx = 3; gbc.gridwidth = 1;
    leftPanel.add(new JLabel("Tiền dịch vụ:"), gbc);
    gbc.gridx = 4;
    leftPanel.add(totalService, gbc);

    // Row 2 - Ngày trả
    gbc.gridx = 0; gbc.gridy = 2;
    leftPanel.add(new JLabel("Ngày trả:"), gbc);
    gbc.gridx = 1; gbc.gridwidth = 2;
    leftPanel.add(checkOutField, gbc);

    // Row 2 - Tiền phòng
    gbc.gridx = 3; gbc.gridwidth = 1;
    leftPanel.add(new JLabel("Tiền phòng:"), gbc);
    gbc.gridx = 4;
    leftPanel.add(totalRoom, gbc);

    // Row 3 - Tổng thanh toán
    gbc.gridx = 0; gbc.gridy = 3;
    leftPanel.add(new JLabel("Tổng thanh toán:"), gbc);
    gbc.gridx = 1; gbc.gridwidth = 2;
    leftPanel.add(totalAmountLabel, gbc);

    // ==================== PANEL PHẢI ====================
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setBorder(BorderFactory.createTitledBorder("Sản phẩm - Dịch vụ"));
    rightPanel.setPreferredSize(new Dimension(250, 200)); // đảm bảo không co

    DefaultListModel<Service> model = new DefaultListModel<>();
    for (Service s : new ServiceControl().getAll()) {
        model.addElement(s);
        
    }

    JList<Service> serviceList = new JList<>(model);
    serviceList.setFont(new Font("Arial", Font.PLAIN, 14));
    serviceList.setFixedCellHeight(26);

    JScrollPane scrollPane = new JScrollPane(serviceList);
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    // Mouse click để thêm DV
    serviceList.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            Service selected = serviceList.getSelectedValue();
            if (selected == null) return;

            String name = selected.getName();
            double price = selected.getPrice();

            boolean found = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (name.equalsIgnoreCase(tableModel.getValueAt(i, 0).toString())) {
                    int sl = Integer.parseInt(tableModel.getValueAt(i, 1).toString()) + 1;
                    tableModel.setValueAt(sl, i, 1);
                    tableModel.setValueAt(formatPrice(sl * price), i, 3);
                    found = true;
                    break;
                }
            }
            if (!found) {
                tableModel.addRow(new Object[]{name, 1, formatPrice(price), formatPrice(price)});
            }

            calculateTotal();
        }
    });

    // ==================== GỘP 2 PANEL LẠI ====================
    mainPanel.add(leftPanel, BorderLayout.CENTER);
    mainPanel.add(rightPanel, BorderLayout.EAST);

    return mainPanel;
}

    private String formatPrice(double p)
    {
        DecimalFormat df = new DecimalFormat("00,000");
        return df.format(p);
    }


    private JPanel createServicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Sản phẩm - Dịch vụ"));

        // Header với thông tin phòng
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(255, 240, 240));
        String infor =  "Phòng " + slRoom.getNum();
        JLabel roomLabel = new JLabel(infor);
        roomLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roomLabel.setForeground(Color.RED);
        headerPanel.add(roomLabel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(serviceTable), BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("TỔNG THANH TOÁN"));

        JPanel totalInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel totalLabel = new JLabel("TỔNG TIỀN:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(Color.RED);

        totalInfoPanel.add(totalLabel);
        totalInfoPanel.add(Box.createHorizontalStrut(20));
        totalInfoPanel.add(totalAmountLabel);

        panel.add(totalInfoPanel, BorderLayout.WEST);

        return panel;
    }

    private void setupEventHandlers() {
        // Thêm event handler cho việc thay đổi số lượng trong bảng
        serviceTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Cột số lượng
                calculateTotal();
            }
        });
        serviceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Nếu click 2 lần
                if (e.getClickCount() == 1) {
                    int row = serviceTable.getSelectedRow();
                    if (row != -1) {
                        try {
                            int quantity = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
                            double unitPrice = Double.parseDouble(tableModel.getValueAt(row, 2).toString());

                            if (quantity > 1) {
                                quantity--;
                                tableModel.setValueAt(quantity, row, 1);
                                tableModel.setValueAt(formatPrice(quantity * unitPrice), row, 3);
                            } else {
                                tableModel.removeRow(row);
                            }

                            calculateTotal();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        // Thêm event handler cho ComboBox trạng thái
        statusComboBox.addActionListener(e -> {
            String selectedStatus = (String) statusComboBox.getSelectedItem();
            System.out.println("Trạng thái đã chọn: " + selectedStatus);
            // Có thể thêm logic xử lý khác ở đây
        });
        checkInField.getDateEditor().addPropertyChangeListener(e ->
                {
                    if ("date".equals(e.getPropertyName())) {
                    validateAndCalculateDays();
                }
                });
        checkOutField.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName())) {
                validateAndCalculateDays();
            }
        });
        
        JTextField editor = (JTextField) customerCBX.getEditor().getEditorComponent();
        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) { if(!isClick) updateSuggestions(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { 
            if(!isClick) 
            {
                updateSuggestions();
                //isClick = false;
            }
        }
        public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        
        private void updateSuggestions() {
            SwingUtilities.invokeLater(() -> {
                String text = editor.getText().trim();
                if (text.length() == 0)
                {
                    customerCBX.hidePopup();
                    isClick =true;
                    prioritizeMatches(customerNameModel, text);
                    customerNameModel.setSelectedItem(text);
                    isClick = false;
                    return;
                }
                isClick = true;
                prioritizeMatches(customerNameModel, text);
                customerCBX.setSelectedItem(text);
                isClick = false;
                customerCBX.addActionListener(e -> {
                    isClick = true;
                    customerCBX.hidePopup();
                    SwingUtilities.invokeLater(() -> isClick = false);
                    return;
                });
                if (customerCBX.isDisplayable()) {
                    customerCBX.showPopup();
                }
                isClick = false;
            });
        }
        });
        
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Customer cs = (Customer) customerCBX.getSelectedItem();
                java.sql.Date dt_in = new java.sql.Date(checkInField.getDate().getTime());
                java.sql.Date dt_out = new java.sql.Date(checkOutField.getDate().getTime());
                double totalroom = Double.parseDouble(totalRoom.getText().replaceAll("[^0-9]", ""));
                int totalservice = Integer.parseInt(totalService.getText().replace(",", ""));
                double total = totalroom + totalservice;
                int stats = statusComboBox.getSelectedItem().equals("Hoàn tất") ? 0 : 1;
                Bill b = new Bill(idBill, slRoom.getNum(), cs.getId(), dt_in, dt_out, totalroom, totalservice, total, stats);
                if(JOptionPane.showConfirmDialog(rootPane, "Luu hoa don", "Xác nhận", JOptionPane.YES_NO_OPTION) == 0 && (idBill == 0 ? bc.insertBill(b) : bc.uptBill(b)) != 0)
                {
                    int bdID = bc.getRoomBill(slRoom.getNum()).getId();
                    List<BillDetail> lst = new ArrayList<>();
                    List<Service> svl = new ServiceControl().getAll();
                    for (int i = 0 ;i<serviceTable.getModel().getRowCount();i++)
                    {
                        int j = 0;
                        while(!serviceTable.getModel().getValueAt(i, 0).equals(svl.get(j).getName()))
                        {
                            j++;
                        }
                        int srId = svl.get(j).getId();
                        int quan = Integer.parseInt(serviceTable.getModel().getValueAt(i, 1).toString().trim());
                        int ttl = Integer.parseInt(serviceTable.getModel().getValueAt(i, 3).toString().replace(",", "").trim());
                        lst.add(new BillDetail(bdID,srId,quan,ttl));
                    }
                    for (int i=0;i<lst.size();i++)
                    {
                        int rs = idBill == 0 ? new billDetailControl().insertDetail(lst.get(i)) : new billDetailControl().uptBD(lst.get(i));
                    }
                    JOptionPane.showMessageDialog(rootPane, "Lưu thành công");
                    slRoom.setStatus(stats);
                    int s = statusComboBox.getSelectedItem().equals("Hoàn tất") ? new RoomControl().uptRoom(slRoom) : 1;
                    dispose();
                }
                else
                {
                    JOptionPane.showMessageDialog(rootPane, "Lỗi! Lưu thất bại");
                }
            }
        });
        
    }
    
    
    private void prioritizeMatches(DefaultComboBoxModel<Customer> model, String key) {
    key = key.toLowerCase();
    model.removeAllElements();
    
    List<Customer> all = customerControl.getAll();
    List<Customer> matched = new ArrayList<>();
    List<Customer> others = new ArrayList<>();

    for (Customer c : all) {
        String name = c.getName().toLowerCase();
        if (name.contains(key)) {
            matched.add(c);
        } else {
            others.add(c);
        }
    }

    // Ưu tiên phần tử chứa key
    for (Customer c : matched) {
        model.addElement(c);
    }

    // Thêm phần còn lại phía sau
    for (Customer c : others) {
        model.addElement(c);
    }
    }
    
    private boolean Like(String src,String key)
    {
        return src.toLowerCase().contains(key.toLowerCase());
    }
    
    private void validateAndCalculateDays() {
        java.util.Date checkIn = checkInField.getDate();
        java.util.Date checkOut = checkOutField.getDate();

        if (checkIn == null || checkOut == null) {
            return;
        }
        
        // Kiểm tra check-in phải từ hôm nay trở đi
        java.util.Date today = new java.util.Date();
        // Đặt giờ phút giây millis về 0 để so sánh chỉ theo ngày
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        checkIn.setHours(0);
        checkIn.setMinutes(0);
        checkIn.setSeconds(0);

        if (checkIn.before(today)) {
            JOptionPane.showMessageDialog(this, "Ngày đặt phòng phải từ hôm nay trở đi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        long diffMillis = checkOut.getTime() - checkIn.getTime();
        if (diffMillis <= 0) {
            JOptionPane.showMessageDialog(this, "Ngày trả phải sau ngày đặt!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long days = diffMillis / (1000 * 60 * 60 * 24);
        if (days == 0) {
            days = 1;
        }
        Room_typeControl rt = new Room_typeControl();
        
        double roomRate = rt.getPrice(slRoom.getType());
        double totalRoomPrice = roomRate * days;

        totalRoom.setText(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(totalRoomPrice));
        calculateTotal();
    }

    private void calculateTotal() {
        long tongDichVu = 0;

        // Tính tổng tiền dịch vụ từ bảng
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object thanhTienObj = tableModel.getValueAt(i, 3);
            if (thanhTienObj != null && !thanhTienObj.toString().trim().isEmpty()) {
                try {
                    String thanhTienStr = thanhTienObj.toString().replaceAll(",", "");
                    if (!thanhTienStr.isEmpty()) {
                        tongDichVu += Long.parseLong(thanhTienStr);
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua lỗi
                }
            }
        }

        // Cập nhật totalService theo định dạng tiền tệ
        totalService.setText(currencyFormat.format(tongDichVu));

        // Parse lại giá trị từ totalRoom đang hiển thị có thể đã được format
        long tienPhong = 0;
        try {
            String rawText = totalRoom.getText().replaceAll("\\.", "").replaceAll("[^0-9]", "");
            if (!rawText.isEmpty()) {
                tienPhong = Long.parseLong(rawText);
            }
        } catch (NumberFormatException e) {
            tienPhong = 0;
        }

        // Format lại totalRoom luôn
        totalRoom.setText(currencyFormat.format(tienPhong));

        // Tính và hiển thị tổng thanh toán
        long tongThanhToan = tienPhong + tongDichVu;
        totalAmountLabel.setText(currencyFormat.format(tongThanhToan) + " đồng");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new Payment(1).setVisible(true);
        });
    }
}
