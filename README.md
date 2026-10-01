# Áreas Geométricas Constructivas

Aplicación de escritorio desarrollada en Java para construir figuras mediante operaciones booleanas en **2D y 3D**.

## Objetivo

Aplicar el principio fundamental de la geometría constructiva: obtener figuras complejas combinando o restando formas básicas mediante unión, intersección y diferencia.

## Funciones

- Seleccionar y editar las figuras A y B.
- Agregar una tercera figura C de manera opcional.
- Modificar posiciones y dimensiones.
- Aplicar unión, intersección y diferencia.
- Encadenar operaciones como **(A ∪ B) − C**.
- Comparar las figuras originales con el resultado.
- Arrastrar la figura seleccionada en 2D.
- Girar y acercar la vista en 3D.

## Tecnologías

- Java.
- Swing para la interfaz gráfica.
- Java2D para el dibujo.
- `java.awt.geom.Area` para las operaciones entre áreas.
- Representación por vóxeles para las operaciones entre volúmenes.

No requiere librerías externas.

## Requisitos

- JDK 17 o posterior.
- Apache NetBeans para abrir y ejecutar el proyecto.
- Sistema operativo con entorno gráfico.

## Estructura del proyecto

La clase principal es `AreasGeometricasConstructivas`.

## Ejecución en NetBeans

1. Descarga el repositorio mediante **Code → Download ZIP**.
2. Extrae el archivo ZIP.
3. Abre Apache NetBeans.
4. Selecciona **Archivo → Abrir proyecto**.
5. Selecciona la carpeta que contiene `build.xml` y `nbproject`.
6. Abre el proyecto y presiona **F6**.

También puedes abrir la clase principal y ejecutarla con **Shift + F6**.

## Uso de la interfaz

### Figuras 2D

1. Selecciona la pestaña **Áreas 2D**.
2. Elige A o B en el primer menú.
3. Selecciona su forma y modifica sus dimensiones.
4. Elige **Unir**, **Intersectar** o **Restar**.
5. Observa el resultado en verde.

### Tercera figura

Pulsa **+ Agregar figura C**. Aparecerá un segundo grupo de operaciones para combinar el resultado de A y B con C.

Por ejemplo, **(A ∪ B) − C** primero une A y B y después resta C. El botón **− Quitar figura C** permite regresar a dos figuras.

### Sólidos 3D

1. Selecciona la pestaña **Sólidos 3D**.
2. Elige un sólido y modifica sus parámetros.
3. Cambia sus coordenadas X, Y y Z para desplazarlo.
4. Aplica las operaciones deseadas.
5. Arrastra sobre la vista para girarla y utiliza la rueda del mouse para ajustar el zoom.

El botón **Restablecer vista** recupera la orientación y el zoom iniciales.
