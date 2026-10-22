const { app, BrowserWindow } = require("electron");
const path = require("node:path");

function createWindow() {
  const win = new BrowserWindow({
    width: 1440,
    height: 900,
    minWidth: 1024,
    minHeight: 700,
    show: true,
    autoHideMenuBar: true,
    backgroundColor: "#14051f",
    webPreferences: {
      contextIsolation: true,
      sandbox: true
    }
  });

  const frontendPath = app.isPackaged
    ? path.join(process.resourcesPath, "frontend", "index.html")
    : path.join(__dirname, "..", "frontend", "index.html");

  win.loadFile(frontendPath);
}

app.whenReady().then(() => {
  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
