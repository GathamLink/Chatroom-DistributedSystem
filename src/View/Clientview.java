package View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 *  This Class is used to build the GUI for this p2p chat system
 */
public class Clientview {

    //Here create basic components for GUI
    protected JFrame frame;
    protected JPanel settingPanel, messagePanel, buttonPanel;
    protected JSplitPane splitPane;
    protected JScrollPane userPanel, msgscrollPanel;
    protected JTextArea messageTextArea;
    protected JTextField IDField, IPField, portField, messageField, nameField;
    protected JLabel messageLabel;
    protected JButton connectButton, disconnectButton, sendButton, kickButton, statButton, listButton;
    protected JList userList;
    protected DefaultListModel<String> listModel;//listmodel for online user panel

    /**
     *  Constructor for this class
     */
    public Clientview() {
        init();
    }

    private void init() {
        //Create a random ID for user
        String id = "10" + (int)((Math.random()*9+1)*1000);

        //set The title, size and layout for client window
        frame = new JFrame("Client");
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        //Init the panel components
        IPField = new JTextField("192.168.50.254");
        portField = new JTextField("20000");
        IDField = new JTextField(id);
        nameField = new JTextField("LINK");
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        connectButton.setFont(new Font("Arial", Font.BOLD, 10));
        disconnectButton.setFont(new Font("Arial", Font.BOLD, 8));

        //init setting panel
        settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(1, 10));
        settingPanel.add(new JLabel("Client ID: "));
        settingPanel.add(IDField);
        settingPanel.add(new JLabel("Client Name: "));
        settingPanel.add(nameField);
        settingPanel.add(new JLabel("Server IP: "));
        settingPanel.add(IPField);
        settingPanel.add(new JLabel("Port: "));
        settingPanel.add(portField);
        settingPanel.add(connectButton);
        settingPanel.add(disconnectButton);
        settingPanel.setBorder(new TitledBorder("Client Setting"));

        //init Online User panel
        listModel = new DefaultListModel<String>();
        userList = new JList(listModel);
        userPanel = new JScrollPane(userList);
        userPanel.setBorder(new TitledBorder("Online User"));

        //init Receive Message Panel
        messageTextArea = new JTextArea();
        messageTextArea.setEditable(false);
        messageTextArea.setForeground(Color.decode("#228B22"));
        //All the message area into a scroll panel
        msgscrollPanel = new JScrollPane(messageTextArea);
        msgscrollPanel.setBorder(new TitledBorder("Receive Message"));

        //init Send Message panel
        messageLabel = new JLabel("TO: ALL PEOPLE");
        messageField = new JTextField();
        sendButton = new JButton("Send");
        kickButton = new JButton("Kick");
        statButton = new JButton("State");
        listButton = new JButton("Refresh");
        //here is a panel for buttons in the messagePanel
        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(kickButton);
        buttonPanel.add(statButton);
        buttonPanel.add(listButton);
        //here add the components into the messagePanel
        messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageLabel, BorderLayout.WEST);
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.EAST);
        messagePanel.setBorder(new TitledBorder("Send Message"));

        //combine the online user panel and receive message panel together
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel,msgscrollPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(15);
        splitPane.setDividerLocation(120);

        //add all the panels inside the frame
        frame.add(settingPanel, BorderLayout.NORTH);
        frame.add(splitPane,BorderLayout.CENTER);
        frame.add(messagePanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        //init the UI on or close
        ClientUI(false);

    }

    /**
     *  THe method to control whether some UIs are close or open
     * @param on    variable to decide whether components are open/close
     */
    public void ClientUI(boolean on) {
        IDField.setEnabled(!on);
        nameField.setEnabled(!on);
        IPField.setEnabled(!on);
        portField.setEnabled(!on);
        connectButton.setEnabled(!on);
        disconnectButton.setEnabled(on);
        messageField.setEnabled(on);
        sendButton.setEnabled(on);
        kickButton.setEnabled(on);
        statButton.setEnabled(on);
        listButton.setEnabled(on);
    }
}
