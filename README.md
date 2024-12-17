# Proyecto TrackingYou

## Descripción General
TrackingYou es una aplicación de Android desarrollada en Kotlin. Utiliza servicios de Firebase para las operaciones del backend, lo que requiere un archivo válido `google-services.json` para la configuración.

---

## Requisitos Previos
Antes de comenzar, asegúrese de tener instalados los siguientes componentes:

1. **Java Development Kit (JDK)**: Versión 11 o superior.
2. **Android Studio**: Arctic Fox (2020.3.1) o posterior.
3. **Git**: Para clonar el repositorio.
4. **Cuenta de Firebase**: Para descargar el archivo `google-services.json`.

---

## Pasos para Ejecutar el Proyecto

### 1. Clonar el Repositorio

Use el siguiente comando para clonar el repositorio:

```bash
git clone https://github.com/maikyh/TrackYou.git
```

Navegue al directorio del proyecto:

```bash
cd TrackYou
```

---

### 2. Abrir el Proyecto en Android Studio

1. Inicie Android Studio.
2. Haga clic en **File > Open**.
3. Seleccione la carpeta del repositorio clonado y haga clic en **OK**.

---

### 3. Configurar Firebase

1. Vaya a la [Consola de Firebase](https://console.firebase.google.com/).
2. Seleccione su proyecto o cree uno nuevo.
3. Navegue a **Configuración del proyecto > General > Tus aplicaciones**.
4. Agregue una nueva aplicación de Android:
   - **Nombre del paquete**: Asegúrese de que coincida con el `applicationId` en el archivo `app/build.gradle` (por ejemplo, `com.example.trackingyou`).
   - **Apodo de la aplicación**: Opcional.
   - **Certificado SHA-1**: Opcional, pero recomendado para la Autenticación de Firebase y los Enlaces Dinámicos.
5. Descargue el archivo `google-services.json`.
6. Coloque el archivo `google-services.json` en el directorio `app/` del proyecto.

---

### 4. Construir el Proyecto

1. En Android Studio, abra el **Terminal** o use las opciones integradas para ejecutar comandos de Gradle.
2. Limpie el proyecto:
   ```bash
   ./gradlew clean
   ```
3. Construya el proyecto:
   ```bash
   ./gradlew build
   ```

---

### 5. Ejecutar la Aplicación

1. Conecte un dispositivo Android o inicie un emulador de Android.
2. En Android Studio, haga clic en el botón verde **Run** o use el atajo `Shift + F10`.

