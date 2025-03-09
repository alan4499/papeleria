import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StationeryApp extends JFrame {
    private JButton btnAgregarInventario, btnAgregarProducto, btnActualizar, btnPagar, btnBorrarProducto, btnCancelarCompra, btnBuscar;
    private JPanel panelProductos;
    private JLabel lblTotal;
    private JTextField txtBuscar;
    private Map<Integer, Integer> carrito = new HashMap<>(); // Almacena ID y cantidad en el carrito
    private double total = 0.0;

    public StationeryApp() {
        setTitle("StationeryApp - Sistema de Papelería");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de botones superiores
        JPanel panelBotones = new JPanel();
        btnAgregarInventario = new JButton("Agregar Inventario");
        btnAgregarProducto = new JButton("Agregar Producto");
        btnActualizar = new JButton("Actualizar");
        btnBorrarProducto = new JButton("Borrar Producto");
        txtBuscar = new JTextField(15);
        btnBuscar = new JButton("Buscar");

        panelBotones.add(btnAgregarInventario);
        panelBotones.add(btnAgregarProducto);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnBorrarProducto);
        panelBotones.add(new JLabel("Buscar:"));
        panelBotones.add(txtBuscar);
        panelBotones.add(btnBuscar);
        add(panelBotones, BorderLayout.NORTH);

        // Panel de productos (botones dinámicos)
        panelProductos = new JPanel();
        panelProductos.setLayout(new GridLayout(0, 2, 10, 10)); // 2 columnas, espacio entre botones
        JScrollPane scrollPane = new JScrollPane(panelProductos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior (total, pagar y cancelar)
        JPanel panelInferior = new JPanel(new GridLayout(1, 3));
        lblTotal = new JLabel("Total: $0.00", SwingConstants.CENTER);
        btnPagar = new JButton("Pagar");
        btnCancelarCompra = new JButton("Cancelar Compra");

        // Hacer el botón "Pagar" más grande
        btnPagar.setPreferredSize(new Dimension(150, 50));
        btnCancelarCompra.setPreferredSize(new Dimension(150, 50));

        panelInferior.add(lblTotal);
        panelInferior.add(btnPagar);
        panelInferior.add(btnCancelarCompra);
        add(panelInferior, BorderLayout.SOUTH);

        // Acciones de los botones
        btnAgregarInventario.addActionListener(e -> agregarInventario());
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProductos());
        btnPagar.addActionListener(e -> pagar());
        btnBorrarProducto.addActionListener(e -> borrarProducto());
        btnCancelarCompra.addActionListener(e -> cancelarCompra());
        btnBuscar.addActionListener(e -> buscarProductos());

        // Cargar productos iniciales
        actualizarProductos();
    }

    private void agregarInventario() {
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto:");
        String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad a agregar:");

        if (idProducto != null && cantidad != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);
                int cant = Integer.parseInt(cantidad);

                // Actualizar el stock del producto
                String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, cant);
                pstmt.setInt(2, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Inventario actualizado correctamente.");
                    actualizarProductos(); // Refrescar la lista
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el producto con ID: " + id);
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void agregarProducto() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto:");
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto:");
        String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad inicial:");
        String precio = JOptionPane.showInputDialog(this, "Ingrese el precio del producto:");

        if (nombre != null && idProducto != null && cantidad != null && precio != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);
                int cant = Integer.parseInt(cantidad);
                double precioProducto = Double.parseDouble(precio);

                // Insertar el nuevo producto
                String sql = "INSERT INTO productos (id, nombre, precio, stock) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setString(2, nombre);
                pstmt.setDouble(3, precioProducto);
                pstmt.setInt(4, cant);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Producto agregado correctamente.");
                actualizarProductos(); // Refrescar la lista
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void actualizarProductos() {
        panelProductos.removeAll(); // Limpiar el panel de productos
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM productos");

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");

                // Crear un botón para cada producto
                JButton btnProducto = new JButton("<html>" + nombre + "<br>Precio: $" + precio + "<br>Stock: " + stock + "</html>");
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
            for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
                int id = entry.getKey();
                int cantidad = entry.getValue();

                // Verificar si hay suficiente inventario
                String sqlStock = "SELECT stock FROM productos WHERE id = ?";
                PreparedStatement pstmtStock = conn.prepareStatement(sqlStock);
                pstmtStock.setInt(1, id);
                ResultSet rs = pstmtStock.executeQuery();

                if (rs.next()) {
                    int stockActual = rs.getInt("stock");
                    if (stockActual < cantidad) {
                        JOptionPane.showMessageDialog(this, "Cantidad no válida: Falta de inventario para el producto ID " + id);
                        return; // Detener el proceso de pago
                    }
                }
            }

            // Si hay suficiente inventario, proceder con el pago
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

            JOptionPane.showMessageDialog(this, "Pago realizado. Total: $" + String.format("%.2f", total));
            carrito.clear(); // Limpiar el carrito
            total = 0.0; // Reiniciar el total
            lblTotal.setText("Total: $0.00"); // Actualizar la etiqueta
            actualizarProductos(); // Refrescar la lista de productos
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al procesar el pago: " + e.getMessage());
        }
    }

    private void cancelarCompra() {
        carrito.clear(); // Limpiar el carrito
        total = 0.0; // Reiniciar el total
        lblTotal.setText("Total: $0.00"); // Actualizar la etiqueta
        JOptionPane.showMessageDialog(this, "Compra cancelada.");
    }

    private void borrarProducto() {
        String idProducto = JOptionPane.showInputDialog(this, "Ingrese el ID del producto a borrar:");
        if (idProducto != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
                int id = Integer.parseInt(idProducto);

                // Eliminar el producto
                String sql = "DELETE FROM productos WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Producto borrado correctamente.");
                    actualizarProductos(); // Refrescar la lista
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el producto con ID: " + id);
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void buscarProductos() {
        String nombreBuscar = txtBuscar.getText().trim();
        if (!nombreBuscar.isEmpty()) {
            panelProductos.removeAll(); // Limpiar el panel de productos
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/StationeryApp", "usuario2", "1")) {
                String sql = "SELECT * FROM productos WHERE nombre LIKE ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "%" + nombreBuscar + "%");
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    double precio = rs.getDouble("precio");
                    int stock = rs.getInt("stock");

                    // Crear un botón para cada producto
                    JButton btnProducto = new JButton("<html>" + nombre + "<br>Precio: $" + precio + "<br>Stock: " + stock + "</html>");
                    btnProducto.addActionListener(e -> agregarAlCarrito(id, precio));
                    panelProductos.add(btnProducto);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            panelProductos.revalidate(); // Refrescar el panel
            panelProductos.repaint();
        } else {
            actualizarProductos(); // Si no hay texto de búsqueda, mostrar todos los productos
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StationeryApp().setVisible(true));
    }
}