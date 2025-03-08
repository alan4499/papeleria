import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BuenPanApp extends JFrame {
    private JButton btnAgregarInventario, btnAgregarProducto, btnActualizar, btnPagar, btnBorrarProducto;
    private JPanel panelProductos;
    private JLabel lblTotal;
    private Map<Integer, Integer> carrito = new HashMap<>();
    private double total = 0.0;

    public BuenPanApp() {
        setTitle("Buen Pan - Sistema de Panadería");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de botones superiores
        JPanel panelBotones = new JPanel();
        btnAgregarInventario = new JButton("Agregar Inventario");
        btnAgregarProducto = new JButton("Agregar Producto");
        btnActualizar = new JButton("Actualizar");
        btnBorrarProducto = new JButton("Borrar Producto");

        panelBotones.add(btnAgregarInventario);
        panelBotones.add(btnAgregarProducto);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnBorrarProducto);
        add(panelBotones, BorderLayout.NORTH);

        // Panel de productos
        panelProductos = new JPanel();
        panelProductos.setLayout(new GridLayout(0, 3, 10, 10)); // ajusta el tamaño de los botones
        JScrollPane scrollPane = new JScrollPane(panelProductos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior (total y pagar)
        JPanel panelInferior = new JPanel(new GridLayout(1, 2));
        lblTotal = new JLabel("Total: $0.00", SwingConstants.CENTER);
        btnPagar = new JButton("Pagar");

        panelInferior.add(lblTotal);
        panelInferior.add(btnPagar);
        add(panelInferior, BorderLayout.SOUTH);

        // Acciones de los botones
        btnAgregarInventario.addActionListener(e -> agregarInventario());
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProductos());
        btnPagar.addActionListener(e -> pagar());
        btnBorrarProducto.addActionListener(e -> borrarProducto());

        // Cargar productos iniciales
        actualizarProductos();
    }

    private void agregarInventario() {
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto:");
        String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad a agregar:");

        if (idProducto != null && cantidad != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Buen_Pan", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);
                int cant = Integer.parseInt(cantidad);

                // Actualizar el stock del producto
                String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, cant);
                pstmt.setInt(2, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Inventario actualizado correctamente en Buen Pan.");
                    actualizarProductos(); // Refrescar la lista
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el producto con ID: " + id);
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error en Buen Pan: " + e.getMessage());
            }
        }
    }

    private void agregarProducto() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto:");
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto:");
        String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad inicial:");
        String precio = JOptionPane.showInputDialog(this, "Ingrese el precio del producto:");
        String ingredientes = JOptionPane.showInputDialog(this, "Ingrese los ingredientes:");

        if (nombre != null && idProducto != null && cantidad != null && precio != null && ingredientes != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Buen_Pan", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);
                int cant = Integer.parseInt(cantidad);
                double precioProducto = Double.parseDouble(precio);

                // Insertar el nuevo producto
                String sql = "INSERT INTO productos (id, nombre, precio, ingredientes, stock) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setString(2, nombre);
                pstmt.setDouble(3, precioProducto);
                pstmt.setString(4, ingredientes);
                pstmt.setInt(5, cant);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Producto agregado correctamente a Buen Pan.");
                actualizarProductos(); // Refrescar la lista
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error en Buen Pan: " + e.getMessage());
            }
        }
    }

    private void actualizarProductos() {
        panelProductos.removeAll(); // Limpiar el panel de productos
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Buen_Pan", "usuario2", "1")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM productos");

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                String ingredientes = rs.getString("ingredientes");
                int stock = rs.getInt("stock");

                // Crear un botón para cada producto
                JButton btnProducto = new JButton("<html>" + nombre + "<br>Precio: $" + precio + "<br>Stock: " + stock + "<br>Ingredientes: " + ingredientes + "</html>");
                btnProducto.addActionListener(e -> agregarAlCarrito(id, precio));
                panelProductos.add(btnProducto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        panelProductos.revalidate(); // Refrescar el panel
        panelProductos.repaint();
    }

    private void agregarAlCarrito(int id, double precio) {
        String cantidadStr = JOptionPane.showInputDialog(this, "Ingrese la cantidad a comprar:");
        if (cantidadStr != null) {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                carrito.put(id, carrito.getOrDefault(id, 0) + cantidad); // Sumar al carrito
                total += cantidad * precio; // Actualizar el total
                lblTotal.setText("Total: $" + String.format("%.2f", total)); // Mostrar el total
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            }
        }
    }

    private void pagar() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Buen_Pan", "usuario2", "1")) {
            for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
                int id = entry.getKey();
                int cantidad = entry.getValue();

                // Restar del stock
                String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, cantidad);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Pago realizado en Buen Pan. Total: $" + String.format("%.2f", total));
            carrito.clear(); // Limpiar el carrito
            total = 0.0; // Reiniciar el total
            lblTotal.setText("Total: $0.00"); // Actualizar la etiqueta
            actualizarProductos(); // Refrescar la lista de productos
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en Buen Pan al procesar el pago: " + e.getMessage());
        }
    }

    private void borrarProducto() {
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto a borrar:");
        if (idProducto != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Buen_Pan", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);

                // Eliminar el producto
                String sql = "DELETE FROM productos WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Producto borrado correctamente de Buen Pan.");
                    actualizarProductos(); // Refrescar la lista
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el producto con ID: " + id);
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error en Buen Pan: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BuenPanApp().setVisible(true));
    }
}