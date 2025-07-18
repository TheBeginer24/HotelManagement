package View;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import Control.FloorControl;
import DAO.FloorDAO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Model.*;
import java.util.List;

public class FloorManagement extends JFrame {
    
    private JTable floorTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JButton addButton, editButton, deleteButton, settingsButton;
    private ImageIcon addIcon,editIcon,delIcon,setIcon;
    private FloorControl flc = new FloorControl();

    
    public FloorManagement() {
        initComponents();
        setupLayout();
        setupEventHandlers();
        this.setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Danh mục Tầng");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        
        //Icon
        addIcon = new ImageIcon(getClass().getResource("/img/add.png"));
        editIcon = new ImageIcon(getClass().getResource("/img/refresh.png"));
        delIcon = new ImageIcon(getClass().getResource("/img/trash.png"));
        setIcon = new ImageIcon(getClass().getResource("/img/settings.png"));
        
        // Tạo toolbar buttons với icons
        addButton = new JButton();
        addButton.setIcon(addIcon);
        addButton.setToolTipText("Thêm");
        addButton.setPreferredSize(new Dimension(30, 30));
        
        editButton = new JButton();
        editButton.setIcon(editIcon);
        editButton.setToolTipText("Sửa");
        editButton.setPreferredSize(new Dimension(30, 30));
        
        deleteButton = new JButton();
        deleteButton.setIcon(delIcon);
        deleteButton.setToolTipText("Xóa");
        deleteButton.setPreferredSize(new Dimension(30, 30));
        
        settingsButton = new JButton();
        settingsButton.setIcon(setIcon);
        settingsButton.setToolTipText("Thiết lập");
        settingsButton.setPreferredSize(new Dimension(30, 30));
        
        // Tạo bảng dữ liệu
        int countFL = flc.countFloor();
        String[] columnNames = {"STT", "TÊN TẦNG"};
        Object[][] data = new Object[countFL][2] ;
        List<Floor> flList = flc.getAll();
        for (int i=0;i<countFL;i++)
        {
            data[i] = new Object[]{flList.get(i).getId(),flList.get(i).getName()};
        }
        
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa trực tiếp
            }
        };
       
        
        floorTable = new JTable(tableModel);
        floorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        floorTable.getTableHeader().setReorderingAllowed(false);
        
        // Thiết lập độ rộng cột
        floorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        floorTable.getColumnModel().getColumn(0).setMaxWidth(50);
        floorTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        
        // Text field cho thông tin
        nameField = new JTextField();
        nameField.setEnabled(false); // Disabled như trong hình
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolbarPanel.add(addButton);
        toolbarPanel.add(editButton);
        toolbarPanel.add(deleteButton);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel bảng dữ liệu
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Danh sách", 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        
        JScrollPane scrollPane = new JScrollPane(floorTable);
        scrollPane.setPreferredSize(new Dimension(350, 180));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel thông tin
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Thông tin", 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel("Tên:"));
        nameField.setPreferredSize(new Dimension(200, 25));
        fieldPanel.add(nameField);
        
        JLabel statusLabel = new JLabel("Disabled");
        statusLabel.setForeground(Color.GRAY);
        fieldPanel.add(statusLabel);
        
        infoPanel.add(fieldPanel, BorderLayout.NORTH);
        
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Xử lý sự kiện chọn hàng trong bảng
        floorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = floorTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String floorName = (String) tableModel.getValueAt(selectedRow, 1);
                    nameField.setText(floorName);
                }
            }
        });
        
        // Xử lý sự kiện các nút
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFloor();
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editFloor();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFloor();
            }
        });
               
    }
    
    private void addFloor() {
        String floorName = JOptionPane.showInputDialog(this, "Nhập tên tầng:", "Thêm tầng", JOptionPane.PLAIN_MESSAGE);
        if (floorName != null && !floorName.trim().isEmpty()) {
            int rowCount = flc.createNewID();
            tableModel.addRow(new Object[]{rowCount, floorName.trim()});
            flc.insertFloor(new Floor(rowCount,floorName.trim()));
        }
    }
    
    private void editFloor() {
        int selectedRow = floorTable.getSelectedRow();
        if (selectedRow >= 0) {
            String currentName = (String) tableModel.getValueAt(selectedRow, 1);
            String newName = JOptionPane.showInputDialog(this, "Sửa tên tầng:", currentName);
            if (newName != null && !newName.trim().isEmpty()) {
                tableModel.setValueAt(newName.trim(), selectedRow, 1);
                nameField.setText(newName.trim());
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                flc.uptFloor(new Floor(id,newName));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tầng cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteFloor() {
        int selectedRow = floorTable.getSelectedRow();
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa tầng này?", 
                "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                nameField.setText("");
                
                flc.delFloor(id);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tầng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
    
   
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FloorManagement().setVisible(true);
            }
        });
    }
}
