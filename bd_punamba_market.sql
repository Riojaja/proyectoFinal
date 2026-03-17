CREATE DATABASE IF NOT EXISTS bd_punamba_market;
USE bd_punamba_market;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- =========================================
-- TABLAS
-- =========================================

CREATE TABLE categorias (
  id_categoria INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  descripcion TEXT DEFAULT NULL,
  categoria_padre INT(11) DEFAULT NULL,
  icono VARCHAR(255) DEFAULT NULL,
  estado TINYINT(1) DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uk_categoria_nombre (nombre),
  KEY fk_categoria_padre (categoria_padre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
  id_rol INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255) DEFAULT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_rol),
  UNIQUE KEY uk_rol_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios (
  id_usuario INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  tipo_documento ENUM('DNI','CE','RUC','Pasaporte') NOT NULL,
  numero_documento VARCHAR(20) NOT NULL,
  email VARCHAR(150) NOT NULL,
  email_verificado TINYINT(1) DEFAULT 0,
  codigo_verificacion VARCHAR(100) DEFAULT NULL,
  fecha_verificacion TIMESTAMP NULL DEFAULT NULL,
  password_hash VARCHAR(255) NOT NULL,
  telefono VARCHAR(20) DEFAULT NULL,
  fecha_nacimiento DATE DEFAULT NULL,
  estado ENUM('activo','inactivo','suspendido') DEFAULT 'activo',
  fecha_registro TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  ultima_conexion TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuario_documento (numero_documento),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios_roles (
  id_usuario INT(11) NOT NULL,
  id_rol INT(11) NOT NULL,
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_usuario, id_rol),
  KEY fk_ur_rol (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE vendedores (
  id_vendedor INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  nombre_empresa VARCHAR(150) NOT NULL,
  ruc VARCHAR(20) DEFAULT NULL,
  descripcion TEXT DEFAULT NULL,
  logo VARCHAR(255) DEFAULT NULL,
  calificacion_promedio DECIMAL(3,2) DEFAULT 0.00,
  total_ventas INT(11) DEFAULT 0,
  estado ENUM('activo','inactivo','pendiente') DEFAULT 'pendiente',
  fecha_registro TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_vendedor),
  UNIQUE KEY uk_vendedor_usuario (id_usuario),
  UNIQUE KEY uk_vendedor_ruc (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tiendas (
  id_tienda INT(11) NOT NULL AUTO_INCREMENT,
  id_vendedor INT(11) NOT NULL,
  nombre VARCHAR(150) NOT NULL,
  descripcion TEXT DEFAULT NULL,
  banner VARCHAR(255) DEFAULT NULL,
  direccion VARCHAR(255) DEFAULT NULL,
  telefono VARCHAR(20) DEFAULT NULL,
  email VARCHAR(150) DEFAULT NULL,
  estado TINYINT(1) DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_tienda),
  UNIQUE KEY uk_tienda_vendedor (id_vendedor),
  KEY fk_tienda_vendedor (id_vendedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE atributos (
  id_atributo INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  tipo_dato VARCHAR(30) DEFAULT 'select',
  unidad VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (id_atributo),
  UNIQUE KEY uk_atributo_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE atributo_valores (
  id_valor INT(11) NOT NULL AUTO_INCREMENT,
  id_atributo INT(11) NOT NULL,
  valor VARCHAR(80) NOT NULL,
  PRIMARY KEY (id_valor),
  UNIQUE KEY uk_atributo_valor (id_atributo, valor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE marcas (
  id_marca INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  estado TINYINT(1) DEFAULT 1,
  PRIMARY KEY (id_marca),
  UNIQUE KEY uk_marca_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categoria_marca (
  id_categoria INT NOT NULL,
  id_marca INT NOT NULL,
  PRIMARY KEY (id_categoria, id_marca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categoria_atributos (
  id_categoria INT NOT NULL,
  id_atributo INT NOT NULL,
  obligatorio TINYINT(1) DEFAULT 0,
  orden INT DEFAULT 0,
  PRIMARY KEY (id_categoria, id_atributo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE productos (
  id_producto INT(11) NOT NULL AUTO_INCREMENT,
  id_vendedor INT(11) NOT NULL,
  id_categoria INT(11) NOT NULL,
  nombre VARCHAR(200) NOT NULL,
  descripcion TEXT DEFAULT NULL,
  marca VARCHAR(100) DEFAULT NULL,
  modelo VARCHAR(100) DEFAULT NULL,
  precio_base DECIMAL(10,2) DEFAULT NULL,
  calificacion_promedio DECIMAL(3,2) DEFAULT 0.00,
  total_ventas INT(11) DEFAULT 0,
  estado ENUM('activo','inactivo','agotado') DEFAULT 'activo',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (id_producto),
  KEY idx_productos_categoria (id_categoria),
  KEY idx_productos_vendedor (id_vendedor),
  KEY idx_productos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE productos_imagenes (
  id_imagen INT(11) NOT NULL AUTO_INCREMENT,
  id_producto INT(11) NOT NULL,
  url_imagen VARCHAR(255) NOT NULL,
  es_principal TINYINT(1) DEFAULT 0,
  orden INT(11) DEFAULT 0,
  fecha_subida TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_imagen),
  KEY fk_img_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE variantes_producto (
  id_variante INT(11) NOT NULL AUTO_INCREMENT,
  id_producto INT(11) NOT NULL,
  sku VARCHAR(100) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  precio_oferta DECIMAL(10,2) DEFAULT NULL,
  stock INT(11) NOT NULL DEFAULT 0,
  stock_reservado INT(11) NOT NULL DEFAULT 0,
  activo TINYINT(1) DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (id_variante),
  UNIQUE KEY uk_variante_sku (sku),
  KEY idx_variantes_producto (id_producto),
  KEY idx_variantes_sku (sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE variante_atributos (
  id_variante INT(11) NOT NULL,
  id_atributo INT(11) NOT NULL,
  id_valor INT(11) NOT NULL,
  PRIMARY KEY (id_variante, id_atributo),
  KEY fk_va_atributo (id_atributo),
  KEY fk_va_valor (id_valor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ubigeos (
  codigo_ubigeo VARCHAR(6) NOT NULL,
  departamento VARCHAR(50) NOT NULL,
  provincia VARCHAR(50) NOT NULL,
  distrito VARCHAR(50) NOT NULL,
  PRIMARY KEY (codigo_ubigeo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE direcciones_envio (
  id_direccion INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  nombre_destinatario VARCHAR(150) NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  direccion_linea1 VARCHAR(255) NOT NULL,
  direccion_linea2 VARCHAR(255) DEFAULT NULL,
  codigo_ubigeo VARCHAR(6) NOT NULL,
  es_principal TINYINT(1) DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_direccion),
  KEY fk_dir_ubigeo (codigo_ubigeo),
  KEY idx_direcciones_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE carritos (
  id_carrito INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (id_carrito),
  UNIQUE KEY uk_carrito_usuario (id_usuario),
  KEY idx_carrito_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE carrito_items (
  id_item INT(11) NOT NULL AUTO_INCREMENT,
  id_carrito INT(11) NOT NULL,
  id_variante INT(11) NOT NULL,
  cantidad INT(11) NOT NULL DEFAULT 1,
  precio_snapshot DECIMAL(10,2) NOT NULL,
  fecha_agregado TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_item),
  UNIQUE KEY uk_carrito_variante (id_carrito, id_variante),
  KEY fk_ci_variante (id_variante)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE metodos_envio (
  id_metodo_envio INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) DEFAULT NULL,
  tiempo_estimado VARCHAR(50) DEFAULT NULL,
  estado TINYINT(1) DEFAULT 1,
  PRIMARY KEY (id_metodo_envio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE metodos_pago (
  id_metodo_pago INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255) DEFAULT NULL,
  tipo ENUM('tarjeta','transferencia','billetera_digital') NOT NULL,
  icono VARCHAR(255) DEFAULT NULL,
  activo TINYINT(1) DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_metodo_pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE estados_orden (
  id_estado INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255) DEFAULT NULL,
  color VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (id_estado),
  UNIQUE KEY uk_estado_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ordenes (
  id_orden INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  numero_orden VARCHAR(50) NOT NULL,
  id_estado INT(11) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  descuento DECIMAL(10,2) DEFAULT 0.00,
  impuesto DECIMAL(10,2) DEFAULT 0.00,
  costo_envio DECIMAL(10,2) DEFAULT 0.00,
  total DECIMAL(10,2) NOT NULL,
  id_direccion INT(11) NOT NULL,
  notas TEXT DEFAULT NULL,
  fecha_orden TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  fecha_actualizacion TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (id_orden),
  UNIQUE KEY uk_numero_orden (numero_orden),
  KEY fk_orden_estado (id_estado),
  KEY fk_orden_direccion (id_direccion),
  KEY idx_ordenes_usuario (id_usuario),
  KEY idx_ordenes_fecha (fecha_orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ordenes_detalle (
  id_orden_detalle INT(11) NOT NULL AUTO_INCREMENT,
  id_orden INT(11) NOT NULL,
  id_variante INT(11) NOT NULL,
  id_vendedor INT(11) NOT NULL,
  id_metodo_envio INT(11) NOT NULL,
  cantidad INT(11) NOT NULL,
  precio_unitario_snapshot DECIMAL(10,2) NOT NULL,
  comision_plataforma DECIMAL(10,2) DEFAULT 0.00,
  subtotal DECIMAL(10,2) NOT NULL,
  estado_envio VARCHAR(50) DEFAULT NULL,
  numero_seguimiento VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id_orden_detalle),
  KEY fk_od_orden (id_orden),
  KEY fk_od_variante (id_variante),
  KEY fk_od_vendedor (id_vendedor),
  KEY fk_od_envio (id_metodo_envio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transacciones (
  id_transaccion INT(11) NOT NULL AUTO_INCREMENT,
  id_orden INT(11) NOT NULL,
  id_metodo_pago INT(11) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  numero_transaccion VARCHAR(100) DEFAULT NULL,
  estado ENUM('pendiente','completado','fallido','reembolsado') DEFAULT 'pendiente',
  detalles_pago LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(detalles_pago)),
  fecha_transaccion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_transaccion),
  UNIQUE KEY uk_numero_transaccion (numero_transaccion),
  KEY fk_trx_metodo (id_metodo_pago),
  KEY idx_transacciones_orden (id_orden),
  KEY idx_transacciones_nro (numero_transaccion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE favoritos (
  id_favorito INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  id_producto INT(11) NOT NULL,
  fecha_agregado TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_favorito),
  UNIQUE KEY unique_favorito (id_usuario, id_producto),
  KEY fk_fav_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE valoraciones (
  id_valoracion INT(11) NOT NULL AUTO_INCREMENT,
  id_variante INT(11) NOT NULL,
  id_usuario INT(11) NOT NULL,
  id_orden_detalle INT(11) DEFAULT NULL,
  calificacion INT(11) NOT NULL,
  titulo VARCHAR(200) DEFAULT NULL,
  comentario TEXT DEFAULT NULL,
  imagenes LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(imagenes)),
  verificada TINYINT(1) DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_valoracion),
  UNIQUE KEY uk_val_unica (id_variante, id_usuario),
  KEY fk_val_usuario (id_usuario),
  KEY fk_val_od (id_orden_detalle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cupones (
  id_cupon INT(11) NOT NULL AUTO_INCREMENT,
  id_vendedor INT(11) DEFAULT NULL,
  codigo VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255) DEFAULT NULL,
  tipo_descuento ENUM('porcentaje','monto_fijo') NOT NULL,
  valor_descuento DECIMAL(10,2) NOT NULL,
  monto_minimo DECIMAL(10,2) DEFAULT NULL,
  cantidad_maxima_usos INT(11) DEFAULT NULL,
  cantidad_usos INT(11) DEFAULT 0,
  fecha_inicio DATETIME NOT NULL,
  fecha_expiracion DATETIME NOT NULL,
  activo TINYINT(1) DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id_cupon),
  UNIQUE KEY uk_cupon_codigo (codigo),
  KEY fk_cupon_vendedor (id_vendedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE disputas (
  id_disputa INT(11) NOT NULL AUTO_INCREMENT,
  id_orden_detalle INT(11) NOT NULL,
  id_usuario INT(11) NOT NULL,
  motivo VARCHAR(100) NOT NULL,
  descripcion TEXT NOT NULL,
  evidencia_json LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(evidencia_json)),
  estado ENUM('abierta','en_revision','resuelta_reembolso','resuelta_rechazada') DEFAULT 'abierta',
  fecha_apertura TIMESTAMP NOT NULL DEFAULT current_timestamp(),
  fecha_resolucion TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (id_disputa),
  UNIQUE KEY uk_disputa_orden_detalle (id_orden_detalle),
  KEY fk_disp_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE liquidaciones (
  id_liquidacion INT(11) NOT NULL AUTO_INCREMENT,
  id_vendedor INT(11) NOT NULL,
  monto_total_ventas DECIMAL(10,2) NOT NULL,
  comision_retenida DECIMAL(10,2) NOT NULL,
  monto_a_pagar DECIMAL(10,2) NOT NULL,
  estado ENUM('pendiente','procesando','pagado') DEFAULT 'pendiente',
  fecha_corte DATE NOT NULL,
  fecha_pago TIMESTAMP NULL DEFAULT NULL,
  comprobante_transferencia VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id_liquidacion),
  KEY fk_liq_vendedor (id_vendedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE iconos_categoria (
  id_icono INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  clase_css VARCHAR(100) NOT NULL UNIQUE,
  estado TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================
-- RELACIONES
-- =========================================

ALTER TABLE categorias
  ADD CONSTRAINT fk_categoria_padre
  FOREIGN KEY (categoria_padre) REFERENCES categorias(id_categoria) ON DELETE SET NULL;

ALTER TABLE usuarios_roles
  ADD CONSTRAINT fk_ur_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  ADD CONSTRAINT fk_ur_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol);

ALTER TABLE vendedores
  ADD CONSTRAINT fk_vendedor_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE;

ALTER TABLE tiendas
  ADD CONSTRAINT fk_tienda_vendedor FOREIGN KEY (id_vendedor) REFERENCES vendedores(id_vendedor) ON DELETE CASCADE;

ALTER TABLE atributo_valores
  ADD CONSTRAINT fk_av_atributo FOREIGN KEY (id_atributo) REFERENCES atributos(id_atributo) ON DELETE CASCADE;

ALTER TABLE categoria_marca
  ADD CONSTRAINT fk_cm_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
  ADD CONSTRAINT fk_cm_marca FOREIGN KEY (id_marca) REFERENCES marcas(id_marca);

ALTER TABLE categoria_atributos
  ADD CONSTRAINT fk_ca_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
  ADD CONSTRAINT fk_ca_atributo FOREIGN KEY (id_atributo) REFERENCES atributos(id_atributo);

ALTER TABLE productos
  ADD CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
  ADD CONSTRAINT fk_producto_vendedor FOREIGN KEY (id_vendedor) REFERENCES vendedores(id_vendedor) ON DELETE CASCADE;

ALTER TABLE productos_imagenes
  ADD CONSTRAINT fk_img_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE;

ALTER TABLE variantes_producto
  ADD CONSTRAINT fk_var_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE;

ALTER TABLE variante_atributos
  ADD CONSTRAINT fk_va_variante FOREIGN KEY (id_variante) REFERENCES variantes_producto(id_variante) ON DELETE CASCADE,
  ADD CONSTRAINT fk_va_atributo FOREIGN KEY (id_atributo) REFERENCES atributos(id_atributo),
  ADD CONSTRAINT fk_va_valor FOREIGN KEY (id_valor) REFERENCES atributo_valores(id_valor);

ALTER TABLE direcciones_envio
  ADD CONSTRAINT fk_dir_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  ADD CONSTRAINT fk_dir_ubigeo FOREIGN KEY (codigo_ubigeo) REFERENCES ubigeos(codigo_ubigeo);

ALTER TABLE carritos
  ADD CONSTRAINT fk_car_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE;

ALTER TABLE carrito_items
  ADD CONSTRAINT fk_ci_carrito FOREIGN KEY (id_carrito) REFERENCES carritos(id_carrito) ON DELETE CASCADE,
  ADD CONSTRAINT fk_ci_variante FOREIGN KEY (id_variante) REFERENCES variantes_producto(id_variante);

ALTER TABLE ordenes
  ADD CONSTRAINT fk_orden_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  ADD CONSTRAINT fk_orden_estado FOREIGN KEY (id_estado) REFERENCES estados_orden(id_estado),
  ADD CONSTRAINT fk_orden_direccion FOREIGN KEY (id_direccion) REFERENCES direcciones_envio(id_direccion);

ALTER TABLE ordenes_detalle
  ADD CONSTRAINT fk_od_orden FOREIGN KEY (id_orden) REFERENCES ordenes(id_orden) ON DELETE CASCADE,
  ADD CONSTRAINT fk_od_variante FOREIGN KEY (id_variante) REFERENCES variantes_producto(id_variante),
  ADD CONSTRAINT fk_od_vendedor FOREIGN KEY (id_vendedor) REFERENCES vendedores(id_vendedor),
  ADD CONSTRAINT fk_od_envio FOREIGN KEY (id_metodo_envio) REFERENCES metodos_envio(id_metodo_envio);

ALTER TABLE transacciones
  ADD CONSTRAINT fk_trx_orden FOREIGN KEY (id_orden) REFERENCES ordenes(id_orden),
  ADD CONSTRAINT fk_trx_metodo FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id_metodo_pago);

ALTER TABLE favoritos
  ADD CONSTRAINT fk_fav_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  ADD CONSTRAINT fk_fav_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE;

ALTER TABLE valoraciones
  ADD CONSTRAINT fk_val_variante FOREIGN KEY (id_variante) REFERENCES variantes_producto(id_variante) ON DELETE CASCADE,
  ADD CONSTRAINT fk_val_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  ADD CONSTRAINT fk_val_od FOREIGN KEY (id_orden_detalle) REFERENCES ordenes_detalle(id_orden_detalle) ON DELETE SET NULL;

ALTER TABLE cupones
  ADD CONSTRAINT fk_cupon_vendedor FOREIGN KEY (id_vendedor) REFERENCES vendedores(id_vendedor) ON DELETE CASCADE;

ALTER TABLE disputas
  ADD CONSTRAINT fk_disp_od FOREIGN KEY (id_orden_detalle) REFERENCES ordenes_detalle(id_orden_detalle) ON DELETE CASCADE,
  ADD CONSTRAINT fk_disp_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE;

ALTER TABLE liquidaciones
  ADD CONSTRAINT fk_liq_vendedor FOREIGN KEY (id_vendedor) REFERENCES vendedores(id_vendedor);




-- Insertar roles
INSERT INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Administrador total'),
('VENDEDOR', 'Vendedor'),
('CLIENTE', 'Cliente');

-- Insertar categorías principales
INSERT INTO categorias (nombre, descripcion) VALUES 
('Electrónica', 'Productos electrónicos y tecnología'),
('Hogar', 'Artículos para el hogar'),
('Moda', 'Ropa y accesorios');

-- Insertar algunas marcas básicas
INSERT INTO marcas (nombre) VALUES 
('Samsung'), ('LG'), ('Sony'), ('Oster'), ('Tramontina'), 
('Adidas'), ('Nike'), ('Levis');

-- Insertar métodos de envío
INSERT INTO metodos_envio (nombre, descripcion, tiempo_estimado) VALUES 
('Olva Courier', 'Envío a nivel nacional', '3-5 días'),
('Punamba Express', 'Envío exprés', '24 horas');

-- Insertar estados de orden
INSERT INTO estados_orden (nombre, descripcion, color) VALUES 
('PENDIENTE', 'Orden pendiente de pago', '#FFA500'),
('PAGADO', 'Pago confirmado', '#28A745'),
('ENVIADO', 'Producto enviado', '#007BFF'),
('ENTREGADO', 'Pedido entregado', '#6F42C1');

INSERT INTO usuarios (nombre, apellido, tipo_documento, numero_documento, email, password_hash) 
VALUES ('Admin', 'Punamba', 'DNI', '00000001', 'admin@punamba.com', '$2a$10$AxRW6bvhvEotmeVx5nutHOMYOFZi5dQhdLyUqvYayOxedw69ruzs.');

-- Asignar rol admin
INSERT INTO usuarios_roles (id_usuario, id_rol) 
VALUES (1, 1); -- Asumiendo que ADMIN tiene id_rol = 1

select * from usuarios



INSERT INTO marcas (nombre, estado) VALUES
('Samsung', 1),
('Sony', 1),
('LG', 1),
('JBL', 1),
('TP-Link', 1),
('Oster', 1),
('Tramontina', 1),
('Casa & Confort', 1),
('Suave Hogar', 1),
('Electrolux', 1),
('Adidas', 1),
('Levis', 1),
('Zara', 1),
('Columbia', 1),
('Nike', 1),
('Renzo Costa', 1);

-- =========================================
-- INSERTAR ATRIBUTOS PARA ELECTRÓNICA
-- =========================================
INSERT INTO atributos (id_atributo, nombre, tipo_dato, unidad) VALUES
(1, 'RAM', 'select', 'GB'),
(2, 'Almacenamiento', 'select', 'GB'),
(3, 'Procesador', 'select', NULL),
(4, 'Pantalla', 'select', 'pulgadas'),
(5, 'Color', 'select', NULL),
(6, 'Resolución', 'select', NULL);

-- =========================================
-- INSERTAR ATRIBUTOS PARA HOGAR
-- =========================================
INSERT INTO atributos (id_atributo, nombre, tipo_dato, unidad) VALUES
(7, 'Material', 'select', NULL),
(8, 'Color', 'select', NULL),
(9, 'Capacidad', 'select', 'L'),
(10, 'Potencia', 'number', 'W'),
(11, 'Dimensiones', 'text', 'cm');

-- =========================================
-- INSERTAR ATRIBUTOS PARA MODA
-- =========================================
INSERT INTO atributos (id_atributo, nombre, tipo_dato, unidad) VALUES
(12, 'Talla', 'select', NULL),
(13, 'Color', 'select', NULL),
(14, 'Material', 'select', NULL),
(15, 'Género', 'select', NULL),
(16, 'Estilo', 'select', NULL),
(17, 'Temporada', 'select', NULL);

-- =========================================
-- INSERTAR VALORES DE ATRIBUTOS
-- =========================================

-- RAM (id_atributo = 1)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(1, '4 GB'),
(1, '6 GB'),
(1, '8 GB'),
(1, '12 GB'),
(1, '16 GB'),
(1, '32 GB');

-- Almacenamiento (id_atributo = 2)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(2, '64 GB'),
(2, '128 GB'),
(2, '256 GB'),
(2, '512 GB'),
(2, '1 TB'),
(2, '2 TB');

-- Procesador (id_atributo = 3)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(3, 'Snapdragon 8 Gen 2'),
(3, 'Exynos 1380'),
(3, 'Intel Core i5'),
(3, 'Intel Core i7'),
(3, 'Apple M2'),
(3, 'MediaTek Dimensity');

-- Pantalla (id_atributo = 4)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(4, '6.4"'),
(4, '6.8"'),
(4, '11"'),
(4, '15.6"'),
(4, '27"'),
(4, '55"');

-- Color Electrónica (id_atributo = 5)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(5, 'Negro'),
(5, 'Blanco'),
(5, 'Plata'),
(5, 'Grafito'),
(5, 'Azul'),
(5, 'Dorado');

-- Material Hogar (id_atributo = 7)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(7, 'Acero inoxidable'),
(7, 'Vidrio'),
(7, 'Plástico'),
(7, 'Aluminio'),
(7, 'Cerámica'),
(7, 'Algodón'),
(7, 'Microfibra');

-- Color Hogar (id_atributo = 8)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(8, 'Blanco'),
(8, 'Negro'),
(8, 'Plateado'),
(8, 'Rojo'),
(8, 'Azul'),
(8, 'Beige'),
(8, 'Gris');

-- Capacidad (id_atributo = 9)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(9, '1 L'),
(9, '1.5 L'),
(9, '2 L'),
(9, '2.2 L'),
(9, '3 L'),
(9, '5 L'),
(9, '10 L');

-- Potencia (id_atributo = 10) - No necesita valores, es tipo number

-- Talla Ropa (id_atributo = 12)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(12, 'XS'),
(12, 'S'),
(12, 'M'),
(12, 'L'),
(12, 'XL'),
(12, 'XXL'),
(12, 'Única');

-- Talla Calzado (id_atributo = 12)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(12, '38'),
(12, '39'),
(12, '40'),
(12, '41'),
(12, '42'),
(12, '43'),
(12, '44');

-- Color Moda (id_atributo = 13)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(13, 'Negro'),
(13, 'Blanco'),
(13, 'Azul'),
(13, 'Rojo'),
(13, 'Gris'),
(13, 'Marrón'),
(13, 'Multicolor'),
(13, 'Rosa');

-- Material Moda (id_atributo = 14)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(14, 'Algodón'),
(14, 'Poliéster'),
(14, 'Nailon'),
(14, 'Cuero'),
(14, 'Mezclilla'),
(14, 'Tejido'),
(14, 'Sintético');

-- Género (id_atributo = 15)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(15, 'Hombre'),
(15, 'Mujer'),
(15, 'Unisex'),
(15, 'Niño'),
(15, 'Niña');

-- Estilo (id_atributo = 16)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(16, 'Casual'),
(16, 'Deportivo'),
(16, 'Formal'),
(16, 'Urbano'),
(16, 'Elegante'),
(16, 'Boho');

-- Temporada (id_atributo = 17)
INSERT INTO atributo_valores (id_atributo, valor) VALUES
(17, 'Verano'),
(17, 'Invierno'),
(17, 'Todo tiempo'),
(17, 'Primavera'),
(17, 'Otoño');

-- =========================================
-- ASIGNAR MARCAS A CATEGORÍAS
-- (Asumiendo Electrónica=1, Hogar=2, Moda=3)
-- =========================================

-- Electrónica (id_categoria = 1)
INSERT INTO categoria_marca (id_categoria, id_marca) VALUES
(1, 1), -- Samsung
(1, 2), -- Sony
(1, 3), -- LG
(1, 4), -- JBL
(1, 5); -- TP-Link

-- Hogar (id_categoria = 2)
INSERT INTO categoria_marca (id_categoria, id_marca) VALUES
(2, 6),  -- Oster
(2, 7),  -- Tramontina
(2, 8),  -- Casa & Confort
(2, 9),  -- Suave Hogar
(2, 10); -- Electrolux

-- Moda (id_categoria = 3)
INSERT INTO categoria_marca (id_categoria, id_marca) VALUES
(3, 11), -- Adidas
(3, 12), -- Levis
(3, 13), -- Zara
(3, 14), -- Columbia
(3, 15), -- Nike
(3, 16); -- Renzo Costa

-- =========================================
-- ASIGNAR ATRIBUTOS A CATEGORÍAS
-- =========================================

-- Electrónica (id_categoria = 1)
INSERT INTO categoria_atributos (id_categoria, id_atributo, obligatorio, orden) VALUES
(1, 1, 1, 1),  -- RAM (obligatorio)
(1, 2, 1, 2),  -- Almacenamiento (obligatorio)
(1, 3, 0, 3),  -- Procesador (opcional)
(1, 4, 0, 4),  -- Pantalla (opcional)
(1, 5, 0, 5),  -- Color (opcional)
(1, 6, 0, 6);  -- Resolución (opcional)

-- Hogar (id_categoria = 2)
INSERT INTO categoria_atributos (id_categoria, id_atributo, obligatorio, orden) VALUES
(2, 7, 1, 1),  -- Material (obligatorio)
(2, 8, 0, 2),  -- Color (opcional)
(2, 9, 0, 3),  -- Capacidad (opcional)
(2, 10, 0, 4), -- Potencia (opcional)
(2, 11, 0, 5); -- Dimensiones (opcional)

-- Moda (id_categoria = 3)
INSERT INTO categoria_atributos (id_categoria, id_atributo, obligatorio, orden) VALUES
(3, 12, 1, 1), -- Talla (obligatorio)
(3, 13, 1, 2), -- Color (obligatorio)
(3, 14, 0, 3), -- Material (opcional)
(3, 15, 0, 4), -- Género (opcional)
(3, 16, 0, 5), -- Estilo (opcional)
(3, 17, 0, 6); -- Temporada (opcional)




select * from usuarios