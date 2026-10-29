
import os
import tempfile
import subprocess
from pathlib import Path
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import FileResponse
from starlette.background import BackgroundTask

app = FastAPI()
MAX_SIZE = 20 * 1024 * 1024

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/convert")
async def convert(file: UploadFile = File(...)):
    if not (file.filename or "").lower().endswith(".dwg"):
        raise HTTPException(400, "DWG files only")

    folder = tempfile.TemporaryDirectory()
    source = Path(folder.name) / "input.dwg"
    output = Path(folder.name) / "output.dxf"

    try:
        with open(source, "wb") as f:
            size = 0
            while chunk := await file.read(1024 * 1024):
                size += len(chunk)
                if size > MAX_SIZE:
                    raise HTTPException(413, "File too large")
                f.write(chunk)

        result = subprocess.run(
            ["dwgread", "-O", "DXF", "-o",
             str(output), str(source)],
            capture_output=True,
            timeout=60,
            check=False
        )

        if result.returncode != 0 or not output.exists():
            raise HTTPException(422, "DWG conversion failed")

        return FileResponse(
            output,
            filename="converted.dxf",
            media_type="application/dxf",
            background=BackgroundTask(folder.cleanup)
        )
    except Exception:
        folder.cleanup()
        raise
