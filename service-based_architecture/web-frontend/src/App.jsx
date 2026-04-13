import {useCallback, useEffect, useMemo, useState} from "react";
import axios from "axios";

const GATEWAY_URL = import.meta.env["VITE_GATEWAY_URL"]
const USER_URL = `${GATEWAY_URL}/users`;
const FOOD_URL = `${GATEWAY_URL}/foods`;
const ORDER_URL = `${GATEWAY_URL}/orders`;
const PAYMENT_URL = `${GATEWAY_URL}/payments`;

export const userApi = axios.create({baseURL: USER_URL});
export const foodApi = axios.create({baseURL: FOOD_URL});
export const orderApi = axios.create({baseURL: ORDER_URL});
export const paymentApi = axios.create({baseURL: PAYMENT_URL});

export function getStoredAuth() {
    const raw = localStorage.getItem("minifood_auth");
    return raw ? JSON.parse(raw) : null;
}

export function setStoredAuth(data) {
    if (data) {
        localStorage.setItem("minifood_auth", JSON.stringify(data));
    } else {
        localStorage.removeItem("minifood_auth");
    }
}

function formatMoney(n) {
    if (n == null) return "—";
    return new Intl.NumberFormat("vi-VN", {style: "currency", currency: "VND"}).format(Number(n));
}

export default function App() {
    const [auth, setAuth] = useState(() => getStoredAuth());
    const [tab, setTab] = useState("menu");
    const [msg, setMsg] = useState(null);
    const [err, setErr] = useState(null);

    const [loginUser, setLoginUser] = useState("");
    const [loginPass, setLoginPass] = useState("");
    const [regUser, setRegUser] = useState("");
    const [regPass, setRegPass] = useState("");

    const [foods, setFoods] = useState([]);
    const [cart, setCart] = useState([]);
    const [orders, setOrders] = useState([]);

    const [foodForm, setFoodForm] = useState({name: "", price: "", description: ""});
    const [editingFoodId, setEditingFoodId] = useState(null);

    const [payOrderId, setPayOrderId] = useState(null);
    const [payMethod, setPayMethod] = useState("COD");

    const isAdmin = auth?.role === "ADMIN";

    const showMsg = (text) => {
        setErr(null);
        setMsg(text);
        setTimeout(() => setMsg(null), 5000);
    };

    const showErr = (e) => {
        setMsg(null);
        const d = e?.response?.data;
        let m = d?.message;
        if (Array.isArray(m)) m = m.join(", ");
        if (!m) m = d?.error || e?.response?.statusText || e?.message || e?.data;
        setErr(typeof m === "string" ? m : JSON.stringify(m ?? "Có lỗi xảy ra"));
    };

    const loadFoods = useCallback(async () => {
        const {data} = await foodApi.get("");
        setFoods(data);
    }, []);

    const loadOrders = useCallback(async () => {
        if (!auth?.userId) return;
        const {data} = await orderApi.get("", {headers: {Authorization: `Bearer ${auth.token}`}});
        setOrders(data);
    }, [auth?.userId]);

    useEffect(() => {
        loadFoods().catch(() => showErr({message: "Không tải được món ăn (Food Service)"}));
    }, [loadFoods]);

    useEffect(() => {
        if (auth?.userId) {
            loadOrders().catch(() => {
            });
        } else {
            setOrders([]);
        }
    }, [auth, loadOrders]);

    const cartTotal = useMemo(
        () => cart.reduce((s, i) => s + Number(i.price) * i.quantity, 0),
        [cart]
    );

    const addToCart = (food) => {
        setCart((c) => {
            const x = c.find((i) => i.foodId === food.id);
            if (x) {
                return c.map((i) => (i.foodId === food.id ? {...i, quantity: i.quantity + 1} : i));
            }
            return [...c, {foodId: food.id, name: food.name, price: food.price, quantity: 1}];
        });
    };

    const updateQty = (foodId, delta) => {
        setCart((c) =>
            c
                .map((i) => (i.foodId === foodId ? {...i, quantity: i.quantity + delta} : i))
                .filter((i) => i.quantity > 0)
        );
    };

    const removeLine = (foodId) => setCart((c) => c.filter((i) => i.foodId !== foodId));

    const handleLogin = async (e) => {
        e.preventDefault();
        setErr(null);
        try {
            const {data} = await userApi.post("/login", {
                username: loginUser,
                password: loginPass
            });
            const next = {
                token: data.token,
                userId: data.userId,
                username: data.username,
                role: data.role,
            };
            setAuth(next);
            setStoredAuth(next);
            showMsg(`Chào ${data.username}!`);
            setTab("menu");
        } catch (e) {
            showErr(e);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setErr(null);
        try {
            const {data} = await userApi.post("/register", {username: regUser, password: regPass});
            const next = {
                token: data.token,
                userId: data.userId,
                username: data.username,
                role: data.role,
            };
            setAuth(next);
            setStoredAuth(next);
            showMsg("Đăng ký thành công.");
            setTab("menu");
        } catch (ex) {
            showErr(ex.response);
        }
    };

    const logout = () => {
        setAuth(null);
        setStoredAuth(null);
        setCart([]);
        setTab("menu");
    };

    const placeOrder = async () => {
        if (!auth) {
            setTab("auth");
            return;
        }
        if (!cart.length) return;
        setErr(null);
        try {
            const {data} = await orderApi.post("", {
                userId: auth.userId,
                items: cart.map((i) => ({foodId: i.foodId, quantity: i.quantity})),
            }, {headers: {Authorization: `Bearer ${auth.token}`}});
            setCart([]);
            await loadOrders();
            showMsg(`Đã tạo đơn #${data.id}. Chuyển sang tab Đơn hàng để thanh toán.`);
            setTab("orders");
        } catch (e) {
            showErr(e);
        }
    };

    const submitPayment = async (e) => {
        e.preventDefault();
        if (!payOrderId) return;
        setErr(null);
        try {
            await paymentApi.post("", {
                orderId: Number(payOrderId),
                method: payMethod,
            }, {headers: {Authorization: `Bearer ${auth.token}`}});
            setPayOrderId(null);
            await loadOrders();
            showMsg("Thanh toán thành công. Xem log Payment Service để thấy thông báo.");
        } catch (e) {
            showErr(e);
        }
    };

    const saveFood = async (e) => {
        e.preventDefault();
        setErr(null);
        try {
            const body = {
                name: foodForm.name.trim(),
                price: Number(foodForm.price),
                description: foodForm.description || "",
            };
            if (editingFoodId) {
                await foodApi.put(`${editingFoodId}`, body, {headers: {Authorization: `Bearer ${auth.token}`}});
                showMsg("Đã cập nhật món.");
            } else {
                await foodApi.post("", body, {headers: {Authorization: `Bearer ${auth.token}`}});
                showMsg("Đã thêm món.");
            }
            setFoodForm({name: "", price: "", description: ""});
            setEditingFoodId(null);
            await loadFoods();
        } catch (e) {
            showErr(e);
        }
    };

    const startEditFood = (f) => {
        setEditingFoodId(f.id);
        setFoodForm({name: f.name, price: String(f.price), description: f.description || ""});
        setTab("admin");
    };

    const deleteFood = async (id) => {
        if (!confirm("Xóa món này?")) return;
        setErr(null);
        try {
            await foodApi.delete(`${id}`, {headers: {Authorization: `Bearer ${auth.token}`}});
            await loadFoods();
            showMsg("Đã xóa.");
        } catch (e) {
            showErr(e);
        }
    };

    return (
        <>
            <header style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                marginBottom: "1.5rem"
            }}>
                <div>
                    <h1 style={{margin: 0, fontSize: "1.65rem"}}>Mini Food</h1>
                    <p style={{margin: "0.25rem 0 0", color: "var(--muted)", fontSize: "0.9rem"}}>
                        Đặt món nội bộ — Service-Based Architecture
                    </p>
                </div>
                <div style={{display: "flex", alignItems: "center", gap: "0.75rem", flexWrap: "wrap"}}>
                    {auth ? (
                        <>
                            <span className="badge">{auth.username}</span>
                            {isAdmin && <span className="badge admin">Admin</span>}
                            <button type="button" className="btn btn-ghost" onClick={logout}>
                                Đăng xuất
                            </button>
                        </>
                    ) : (
                        <button type="button" className="btn btn-primary" onClick={() => setTab("auth")}>
                            Đăng nhập
                        </button>
                    )}
                </div>
            </header>

            <nav
                style={{
                    display: "flex",
                    gap: "0.5rem",
                    flexWrap: "wrap",
                    marginBottom: "1.25rem",
                    borderBottom: "1px solid var(--border)",
                    paddingBottom: "0.75rem",
                }}
            >
                {[
                    ["menu", "Thực đơn"],
                    ["cart", `Giỏ (${cart.length})`],
                    ["orders", "Đơn hàng"],
                    ...(isAdmin ? [["admin", "Quản lý món"]] : []),
                    ...(!auth ? [["auth", "Đăng ký / Đăng nhập"]] : []),
                ].map(([k, label]) => (
                    <button
                        key={k}
                        type="button"
                        className="btn btn-ghost"
                        onClick={() => setTab(k)}
                        style={{
                            opacity: tab === k ? 1 : 0.75,
                            borderColor: tab === k ? "var(--accent)" : "var(--border)",
                        }}
                    >
                        {label}
                    </button>
                ))}
            </nav>

            {msg && <div className="alert ok">{msg}</div>}
            {err && <div className="alert err">{err}</div>}

            {tab === "auth" && (
                <div style={{
                    display: "grid",
                    gap: "1.25rem",
                    gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))"
                }}>
                    <div className="card">
                        <h2 style={{marginTop: 0}}>Đăng nhập</h2>
                        <form onSubmit={handleLogin}>
                            <div className="field">
                                <label>Tên đăng nhập</label>
                                <input value={loginUser} onChange={(e) => setLoginUser(e.target.value)} required/>
                            </div>
                            <div className="field">
                                <label>Mật khẩu</label>
                                <input
                                    type="password"
                                    value={loginPass}
                                    onChange={(e) => setLoginPass(e.target.value)}
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">
                                Đăng nhập
                            </button>
                        </form>
                        <p style={{fontSize: "0.8rem", color: "var(--muted)", marginTop: "1rem"}}>
                            Demo admin: <code>admin</code> / <code>admin123</code>
                        </p>
                    </div>
                    <div className="card">
                        <h2 style={{marginTop: 0}}>Đăng ký</h2>
                        <form onSubmit={handleRegister}>
                            <div className="field">
                                <label>Tên đăng nhập</label>
                                <input value={regUser} onChange={(e) => setRegUser(e.target.value)} required
                                       minLength={3}/>
                            </div>
                            <div className="field">
                                <label>Mật khẩu</label>
                                <input
                                    type="password"
                                    value={regPass}
                                    onChange={(e) => setRegPass(e.target.value)}
                                    required
                                    minLength={4}
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">
                                Đăng ký
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {tab === "menu" && (
                <div className="grid-foods">
                    {foods.map((f) => (
                        <div key={f.id} className="card"
                             style={{display: "flex", flexDirection: "column", gap: "0.75rem"}}>
                            <div>
                                <h3 style={{margin: 0}}>{f.name}</h3>
                                <p style={{margin: "0.35rem 0 0", color: "var(--accent)", fontWeight: 600}}>
                                    {formatMoney(f.price)}
                                </p>
                                {f.description && (
                                    <p style={{
                                        margin: "0.5rem 0 0",
                                        fontSize: "0.85rem",
                                        color: "var(--muted)"
                                    }}>{f.description}</p>
                                )}
                            </div>
                            <div style={{display: "flex", gap: "0.5rem", marginTop: "auto", flexWrap: "wrap"}}>
                                <button type="button" className="btn btn-primary" onClick={() => addToCart(f)}>
                                    Thêm vào giỏ
                                </button>
                                {isAdmin && (
                                    <>
                                        <button type="button" className="btn btn-ghost"
                                                onClick={() => startEditFood(f)}>
                                            Sửa
                                        </button>
                                        <button type="button" className="btn btn-danger"
                                                onClick={() => deleteFood(f.id)}>
                                            Xóa
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {tab === "cart" && (
                <div className="card">
                    <h2 style={{marginTop: 0}}>Giỏ hàng</h2>
                    {!cart.length && <p style={{color: "var(--muted)"}}>Chưa có món nào.</p>}
                    {cart.map((i) => (
                        <div
                            key={i.foodId}
                            style={{
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                                gap: "1rem",
                                padding: "0.65rem 0",
                                borderBottom: "1px solid var(--border)",
                            }}
                        >
                            <div>
                                <strong>{i.name}</strong>
                                <div style={{fontSize: "0.85rem", color: "var(--muted)"}}>
                                    {formatMoney(i.price)} × {i.quantity}
                                </div>
                            </div>
                            <div style={{display: "flex", alignItems: "center", gap: "0.35rem"}}>
                                <button type="button" className="btn btn-ghost" onClick={() => updateQty(i.foodId, -1)}>
                                    −
                                </button>
                                <span>{i.quantity}</span>
                                <button type="button" className="btn btn-ghost" onClick={() => updateQty(i.foodId, 1)}>
                                    +
                                </button>
                                <button type="button" className="btn btn-danger" onClick={() => removeLine(i.foodId)}>
                                    Xóa
                                </button>
                            </div>
                        </div>
                    ))}
                    {!!cart.length && (
                        <>
                            <p style={{marginTop: "1rem", fontSize: "1.1rem"}}>
                                Tổng: <strong>{formatMoney(cartTotal)}</strong>
                            </p>
                            <button type="button" className="btn btn-primary" onClick={placeOrder}>
                                Tạo đơn hàng
                            </button>
                        </>
                    )}
                </div>
            )}

            {tab === "orders" && (
                <div className="card">
                    <h2 style={{marginTop: 0}}>Đơn hàng của tôi</h2>
                    {!orders.length && <p style={{color: "var(--muted)"}}>Chưa có đơn.</p>}
                    {orders.map((o) => (
                        <div
                            key={o.id}
                            style={{
                                border: "1px solid var(--border)",
                                borderRadius: 10,
                                padding: "1rem",
                                marginBottom: "0.75rem",
                                background: "var(--surface2)",
                            }}
                        >
                            <div style={{
                                display: "flex",
                                justifyContent: "space-between",
                                flexWrap: "wrap",
                                gap: "0.5rem"
                            }}>
                                <strong>Đơn #{o.id}</strong>
                                <span className="badge">{o.status}</span>
                            </div>
                            <p style={{margin: "0.35rem 0", fontSize: "0.9rem", color: "var(--muted)"}}>
                                {o.items?.map((it) => `${it.foodName} ×${it.quantity}`).join(", ")}
                            </p>
                            <p style={{margin: 0}}>Tổng: {formatMoney(o.total)}</p>
                            {o.status === "PENDING" && (
                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    style={{marginTop: "0.75rem"}}
                                    onClick={() => {
                                        setPayOrderId(o.id);
                                        setTab("pay");
                                    }}
                                >
                                    Thanh toán
                                </button>
                            )}
                            {o.status === "PAID" && o.paymentMethod && (
                                <p style={{fontSize: "0.85rem", color: "var(--muted)", marginTop: "0.5rem"}}>
                                    Đã thanh toán: {o.paymentMethod}
                                </p>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {tab === "pay" && (
                <div className="card">
                    <h2 style={{marginTop: 0}}>Thanh toán (giả lập)</h2>
                    <button type="button" className="btn btn-ghost" style={{marginBottom: "1rem"}}
                            onClick={() => setTab("orders")}>
                        ← Quay lại đơn hàng
                    </button>
                    <form onSubmit={submitPayment}>
                        <div className="field">
                            <label>Mã đơn</label>
                            <input
                                type="number"
                                value={payOrderId ?? ""}
                                onChange={(e) => setPayOrderId(e.target.value ? Number(e.target.value) : null)}
                                required
                            />
                        </div>
                        <div className="field">
                            <label>Phương thức</label>
                            <select value={payMethod} onChange={(e) => setPayMethod(e.target.value)}>
                                <option value="COD">COD</option>
                                <option value="BANKING">Banking</option>
                            </select>
                        </div>
                        <button type="submit" className="btn btn-primary">
                            Xác nhận thanh toán
                        </button>
                    </form>
                </div>
            )}

            {tab === "admin" && isAdmin && (
                <div className="card">
                    <h2 style={{marginTop: 0}}>{editingFoodId ? "Sửa món" : "Thêm món"}</h2>
                    <form onSubmit={saveFood}>
                        <div className="field">
                            <label>Tên</label>
                            <input value={foodForm.name}
                                   onChange={(e) => setFoodForm({...foodForm, name: e.target.value})} required/>
                        </div>
                        <div className="field">
                            <label>Giá (VND)</label>
                            <input
                                type="number"
                                step="0.01"
                                min="0.01"
                                value={foodForm.price}
                                onChange={(e) => setFoodForm({...foodForm, price: e.target.value})}
                                required
                            />
                        </div>
                        <div className="field">
                            <label>Mô tả</label>
                            <input
                                value={foodForm.description}
                                onChange={(e) => setFoodForm({...foodForm, description: e.target.value})}
                            />
                        </div>
                        <div style={{display: "flex", gap: "0.5rem", flexWrap: "wrap"}}>
                            <button type="submit" className="btn btn-primary">
                                {editingFoodId ? "Cập nhật" : "Thêm"}
                            </button>
                            {editingFoodId && (
                                <button
                                    type="button"
                                    className="btn btn-ghost"
                                    onClick={() => {
                                        setEditingFoodId(null);
                                        setFoodForm({name: "", price: "", description: ""});
                                    }}
                                >
                                    Hủy sửa
                                </button>
                            )}
                        </div>
                    </form>
                </div>
            )}
        </>
    );
}
