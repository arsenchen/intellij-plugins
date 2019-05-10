package com.jetbrains.lang.dart.ide.runner.server.vmService.ui;

import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.dartlang.vm.service.element.ElementList;
import org.dartlang.vm.service.element.IsolateRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

class DartRemoteDebugIsolateJCheckBox extends JCheckBox implements ListCellRenderer {
  public DartRemoteDebugIsolateJCheckBox() {
    super();
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                boolean cellHasFocus) {
    this.setText(value.toString());
    setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
    setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
    this.setSelected(isSelected);
    return this;
  }
}

public class DartRemoteDebugIsolateSelector extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JScrollPane scrollPane;
  private JList<String> myList;
  private static Set<String> selectedIsolateSet = ContainerUtil.newConcurrentSet();

  public DartRemoteDebugIsolateSelector(ElementList<IsolateRef> isolates) {
    setTitle("Select isolates to debug");

    myList = new JBList<>();
    scrollPane.setViewportView(myList);

    if (isolates != null && !isolates.isEmpty()) {
      String[] list = new String[isolates.size()];
      int i = 0;
      for (IsolateRef isolateRef : isolates) {
        list[i] = isolateRef.getName();
        i ++;
      }

      myList.setPreferredSize(new Dimension(320, 240));
      myList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


      myList.setListData(list);
      DartRemoteDebugIsolateJCheckBox cell = new DartRemoteDebugIsolateJCheckBox();
      myList.setCellRenderer(cell);

      myList.setSelectionModel(new DefaultListSelectionModel() {
        @Override
        public void setSelectionInterval(int index0, int index1) {
          if (super.isSelectedIndex(index0)) {
            super.removeSelectionInterval(index0, index1);
          } else {
            super.addSelectionInterval(index0, index1);
          }
        }
      });

      for(int pos = 0; pos < list.length; pos++) {
        if (selectedIsolateSet.contains(list[pos])) {
          myList.setSelectedIndex(pos);
        }
      }
    }

    //selectedIsolateSet.forEach((e) -> myList.setSelectedValue(e, false));

    setLocation(MouseInfo.getPointerInfo().getLocation());

    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectedIsolateSet.clear();
        selectedIsolateSet.addAll(myList.getSelectedValuesList());
        onOK();
      }
    });
  }

  private void onOK() {
    // add your code here
    dispose();
  }

  public static void selectDebugIsolates(ElementList<IsolateRef> isolates) {
    if (isolates == null || isolates.size() <= 1) {
      selectedIsolateSet.clear();
      return;
    }

    DartRemoteDebugIsolateSelector dialog = new DartRemoteDebugIsolateSelector(isolates);
    dialog.pack();
    dialog.setVisible(true);
  }

  public static boolean isDebugIsolate(IsolateRef isolateRef, DartVmServiceDebugProcess debugProcess) {
    if (isolateRef == null) {
      return true;
    }

    if (!debugProcess.isRemoteDebug()) {
      return true;
    }
    return selectedIsolateSet.contains(isolateRef.getName());
  }
}
