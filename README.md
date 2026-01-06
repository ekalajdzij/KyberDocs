# 🔐 KyberDocs  
### Post-Quantum Secure Document Management System (DMS)

**KyberDocs** is a next-generation **Document Management System (DMS)** designed to remain secure in the era of **quantum computing**. The system is built around a **hybrid cryptographic architecture** that combines:

- **CRYSTALS-Kyber-512**, a **NIST-standardized Post-Quantum Key Encapsulation Mechanism (KEM)**, for secure key exchange  
- **AES-256** for high-performance file encryption  

Unlike traditional DMS solutions that rely on **policy-based access control (ACLs)**, KyberDocs enforces **cryptographic access control**. Documents can only be decrypted by users who possess the **mathematically valid private keys**.  
Even **database administrators or system operators cannot access document contents** without the required cryptographic material.

---

## 🧠 Architecture Overview

KyberDocs is composed of two tightly coupled but logically isolated services:

### ☕ Java Spring Boot Backend
- User management, authentication, and authorization (**JWT-based, stateless**)
- AES-based encryption and decryption of files and cryptographic keys
- Secure orchestration of cryptographic workflows
- Persistent storage of encrypted documents and key capsules

### 🐍 Python Cryptographic Microservice (FastAPI)
- Post-quantum key generation (**Kyber-512**)
- Key encapsulation and decapsulation operations
- Isolated cryptographic boundary for Post-Quantum Cryptography (PQC)

🔗 Communication between services is performed via **REST APIs**, ensuring:
- Language isolation  
- Clear separation of concerns  
- Reduced cryptographic attack surface  

---

## 🔁 Secure Document Lifecycle

KyberDocs follows a **strictly enforced cryptographic workflow**:

### 👤 User Registration
- Generates a **Kyber-512 key pair**
- Public keys are stored in plaintext (required for sharing)
- Private keys are **immediately AES-encrypted** and **never stored in raw form**

### 📤 Document Upload
- Each document receives a **unique cryptographic lock**
- Files are encrypted using a randomly generated **AES File Master Key**
- The AES key is **encapsulated using Kyber** and never persisted

### 🤝 Secure Sharing
- Documents are shared by **re-wrapping the File Master Key**
- No file duplication occurs
- Each beneficiary receives a **unique cryptographic access path**

### 📥 Download & Decryption
- Owners and beneficiaries follow **separate cryptographic unlock paths**
- Access is **mathematically enforced**, not policy-based

---

## 🛡️ Why KyberDocs?

### 🔐 Post-Quantum Security
Protects against **“harvest now, decrypt later”** attacks using **NIST-standardized post-quantum cryptography**, ensuring long-term confidentiality of sensitive documents.

### 🔑 Cryptographic Access Control
Eliminates reliance on database permissions and ACLs.  
Access is enforced **purely through cryptography**, not administrative trust.

### 🧾 Strong Auditability & Non-Repudiation
Every sharing action creates a **distinct cryptographic artifact**, providing a verifiable and tamper-resistant access history.

---

> **KyberDocs demonstrates how Post-Quantum Cryptography can be applied in real-world enterprise systems today — not as theory, but as enforceable security architecture.**
