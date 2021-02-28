/*
 * Copyright 2021 Kiritron's Space
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.kiritron.updator;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Киритрон Стэйблкор
 * @version 1.0 Бета
 */

public class frame extends JFrame {
    public JTextArea textarea = new JTextArea();

    public frame() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        final int sizeWidth = 900;
        final int sizeHeight = 550;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int locationX = (screenSize.width - sizeWidth) / 2;
        int locationY = (screenSize.height - sizeHeight) / 2;
        setSize(sizeWidth, sizeHeight);
        setBounds(locationX, locationY, sizeWidth, sizeHeight);

        setTitle("КС Апдэйтор");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel4Buttons = new JPanel();

        textarea.setFont(new Font("Dialog", Font.PLAIN, 14));
        textarea.setEditable(false);
        textarea.setForeground(Color.LIGHT_GRAY);
        textarea.setBackground(Color.DARK_GRAY);

        JButton YesButton = new JButton("Закрыть установщик обновления");
        YesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panel.add(textarea, BorderLayout.CENTER);
        panel4Buttons.add(YesButton);
        panel.add(panel4Buttons, BorderLayout.PAGE_END);

        getContentPane().add(panel, BorderLayout.CENTER);
    }
}
