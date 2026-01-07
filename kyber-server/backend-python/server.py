import subprocess
import json
import hashlib
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI()

origins = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# You should change this path accordingly
KYBER_BACKEND_PATH = "/home/emir/kyber-python-server/kyber_backend"

KYBER_PARAM_SIZES = {
    "kyber512":  {"pk": 800,  "ct": 768,  "sk": 1632},
    "kyber768":  {"pk": 1184, "ct": 1088, "sk": 2400},
    "kyber1024": {"pk": 1568, "ct": 1568, "sk": 3168},
}

class KeygenRequest(BaseModel):
    parameterSet: str

class EncapsulateRequest(BaseModel):
    parameterSet: str
    publicKeyHex: str

class DecapsulateRequest(BaseModel):
    parameterSet: str
    ciphertextHex: str
    secretKeyHex: str

def sha256_hex_from_hex_string(hex_str: str) -> str:
    return hashlib.sha256(bytes.fromhex(hex_str)).hexdigest()

def validate_param_set(param_set: str):
    if param_set not in KYBER_PARAM_SIZES:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported parameterSet '{param_set}', use kyber512|kyber768|kyber1024",
        )

@app.post("/api/kyber/keygen")
def keygen(req: KeygenRequest):
    validate_param_set(req.parameterSet)

    proc = subprocess.run(
        [KYBER_BACKEND_PATH, "keygen", req.parameterSet],
        capture_output=True,
        text=True,
    )

    if proc.returncode != 0:
        raise HTTPException(
            status_code=500,
            detail=proc.stderr.strip() or "kyber_backend keygen failed",
        )

    try:
        data = json.loads(proc.stdout)
    except json.JSONDecodeError:
        raise HTTPException(status_code=500, detail="Invalid JSON from kyber_backend keygen")

    return data

@app.post("/api/kyber/encapsulate")
def encapsulate(req: EncapsulateRequest):
    validate_param_set(req.parameterSet)

    sizes = KYBER_PARAM_SIZES[req.parameterSet]
    pk_hex = req.publicKeyHex.strip()
    expected_pk_hex_len = 2 * sizes["pk"]

    if len(pk_hex) != expected_pk_hex_len:
        raise HTTPException(
            status_code=400,
            detail=f"publicKeyHex must be {expected_pk_hex_len} hex chars for {req.parameterSet}, got {len(pk_hex)}",
        )

    proc = subprocess.run(
        [KYBER_BACKEND_PATH, "encapsulate", req.parameterSet],
        input=pk_hex + "\n",
        capture_output=True,
        text=True,
    )

    if proc.returncode != 0:
        raise HTTPException(
            status_code=500,
            detail=proc.stderr.strip() or "kyber_backend encapsulate failed",
        )

    try:
        data = json.loads(proc.stdout)
    except json.JSONDecodeError:
        raise HTTPException(status_code=500, detail="Invalid JSON from kyber_backend encapsulate")

    if "sharedSecretHex" not in data or "ciphertextHex" not in data:
        raise HTTPException(status_code=500, detail="Missing fields in kyber_backend encapsulate output")

    shared_secret_hash = sha256_hex_from_hex_string(data["sharedSecretHex"])

    return {
        "ciphertextHex": data["ciphertextHex"],
        "sharedSecretHashHex": shared_secret_hash,
    }

@app.post("/api/kyber/decapsulate")
def decapsulate(req: DecapsulateRequest):
    validate_param_set(req.parameterSet)

    ct_hex = req.ciphertextHex.strip()
    sk_hex = req.secretKeyHex.strip()

    sizes = KYBER_PARAM_SIZES[req.parameterSet]
    expected_ct_hex_len = 2 * sizes["ct"]
    expected_sk_hex_len = 2 * sizes["sk"]

    if len(ct_hex) != expected_ct_hex_len:
        raise HTTPException(
            status_code=400,
            detail=f"ciphertextHex must be {expected_ct_hex_len} hex chars for {req.parameterSet}, got {len(ct_hex)}",
        )

    if len(sk_hex) != expected_sk_hex_len:
        raise HTTPException(
            status_code=400,
            detail=f"secretKeyHex must be {expected_sk_hex_len} hex chars for {req.parameterSet}, got {len(sk_hex)}",
        )

    stdin_payload = ct_hex + "\n" + sk_hex + "\n"

    proc = subprocess.run(
        [KYBER_BACKEND_PATH, "decapsulate", req.parameterSet],
        input=stdin_payload,
        capture_output=True,
        text=True,
    )

    if proc.returncode != 0:
        raise HTTPException(
            status_code=500,
            detail=proc.stderr.strip() or "kyber_backend decapsulate failed",
        )

    try:
        data = json.loads(proc.stdout)
    except json.JSONDecodeError:
        raise HTTPException(status_code=500, detail="Invalid JSON from kyber_backend decapsulate")

    if "sharedSecretHex" not in data:
        raise HTTPException(status_code=500, detail="Missing sharedSecretHex in decapsulate output")

    shared_secret_hash = sha256_hex_from_hex_string(data["sharedSecretHex"])

    return {
        "sharedSecretHashHex": shared_secret_hash
    }
