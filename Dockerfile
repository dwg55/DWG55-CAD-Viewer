
FROM debian:trixie-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    python3 python3-venv libredwg-tools && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
RUN pip install --no-cache-dir -r requirements.txt

COPY server.py .
CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8080"]
