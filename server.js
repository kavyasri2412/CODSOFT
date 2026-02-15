const express = require("express");
const cors = require("cors");
const { v4: uuidv4 } = require("uuid");

const app = express();
app.use(cors());
app.use(express.json());

let users = [
    { id: 1, username: "user", pin: "1234", balance: 10000, role: "user", attempts: 0, lockedUntil: null },
    { id: 2, username: "admin", pin: "0000", balance: 0, role: "admin", attempts: 0, lockedUntil: null }
];

let transactions = [];


// 🔐 LOGIN WITH LOCK SYSTEM
app.post("/login", (req, res) => {
    const { username, pin } = req.body;
    const user = users.find(u => u.username === username);

    if (!user) return res.status(401).json({ message: "User not found" });

    if (user.lockedUntil && Date.now() < user.lockedUntil) {
        return res.status(403).json({ message: "Account locked. Try later." });
    }

    if (user.pin !== pin) {
        user.attempts++;
        if (user.attempts >= 3) {
            user.lockedUntil = Date.now() + 60000; // 1 min lock
            user.attempts = 0;
        }
        return res.status(401).json({ message: "Wrong PIN" });
    }

    user.attempts = 0;
    res.json(user);
});


// 💰 GET BALANCE
app.get("/balance/:id", (req, res) => {
    const user = users.find(u => u.id == req.params.id);
    res.json({ balance: user.balance });
});


// 💵 DEPOSIT
app.post("/deposit", (req, res) => {
    const { id, amount } = req.body;
    const user = users.find(u => u.id == id);

    user.balance += amount;

    const txn = {
        txnId: uuidv4(),
        userId: id,
        type: "Deposit",
        amount,
        date: new Date().toLocaleString(),
        balance: user.balance
    };

    transactions.push(txn);
    res.json(txn);
});


// 💸 WITHDRAW
app.post("/withdraw", (req, res) => {
    const { id, amount } = req.body;
    const user = users.find(u => u.id == id);

    if (amount > user.balance)
        return res.status(400).json({ message: "Insufficient balance" });

    user.balance -= amount;

    const txn = {
        txnId: uuidv4(),
        userId: id,
        type: "Withdraw",
        amount,
        date: new Date().toLocaleString(),
        balance: user.balance
    };

    transactions.push(txn);
    res.json(txn);
});


// 🔁 TRANSFER
app.post("/transfer", (req, res) => {
    const { senderId, receiverUsername, amount } = req.body;

    const sender = users.find(u => u.id == senderId);
    const receiver = users.find(u => u.username === receiverUsername);

    if (!receiver) return res.status(404).json({ message: "Receiver not found" });
    if (amount > sender.balance)
        return res.status(400).json({ message: "Insufficient balance" });

    sender.balance -= amount;
    receiver.balance += amount;

    const txn = {
        txnId: uuidv4(),
        userId: senderId,
        type: "Transfer",
        amount,
        to: receiverUsername,
        date: new Date().toLocaleString(),
        balance: sender.balance
    };

    transactions.push(txn);
    res.json(txn);
});


// 📜 FULL TRANSACTION HISTORY
app.get("/transactions/:id", (req, res) => {
    const userTxns = transactions.filter(t => t.userId == req.params.id);
    res.json(userTxns);
});


// 🔑 CHANGE PIN
app.post("/change-pin", (req, res) => {
    const { id, oldPin, newPin } = req.body;
    const user = users.find(u => u.id == id);

    if (user.pin !== oldPin)
        return res.status(400).json({ message: "Old PIN incorrect" });

    user.pin = newPin;
    res.json({ message: "PIN changed successfully" });
});


app.listen(5000, () => {
    console.log("🔥 PRO ATM Server running on http://localhost:5000");
});