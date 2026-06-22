// src/pages/Signup.jsx
//
// Note the role-conditional field (blood_group only for patients) — this is
// the ONE branch of complexity in this file, kept small and local rather
// than spread across multiple components. If this grew much further, the
// next refactor step would be splitting role-specific fields into small
// sub-components — but for 4 roles and one extra field, that's premature.

import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Signup() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "patient",
    phone: "",
    blood_group: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const { signup } = useAuth();
  const navigate = useNavigate();

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await signup(form);
      setSuccess(true);
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container" style={{ maxWidth: 450, marginTop: 60 }}>
      <h2 className="mb-4">Sign Up</h2>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && (
        <div className="alert alert-success">
          Account created! Redirecting to login...
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Name</label>
          <input className="form-control" name="name" value={form.name}
                 onChange={handleChange} required />
        </div>

        <div className="mb-3">
          <label className="form-label">Email</label>
          <input className="form-control" type="email" name="email" value={form.email}
                 onChange={handleChange} required />
        </div>

        <div className="mb-3">
          <label className="form-label">Password</label>
          <input className="form-control" type="password" name="password" value={form.password}
                 onChange={handleChange} required />
        </div>

        <div className="mb-3">
          <label className="form-label">Phone</label>
          <input className="form-control" name="phone" value={form.phone}
                 onChange={handleChange} required />
        </div>

        <div className="mb-3">
          <label className="form-label">Role</label>
          <select className="form-select" name="role" value={form.role} onChange={handleChange}>
            <option value="patient">Patient</option>
            <option value="doctor">Doctor</option>
            <option value="pharmacist">Pharmacist</option>
            <option value="admin">Admin</option>
          </select>
        </div>

        {form.role === "patient" && (
          <div className="mb-3">
            <label className="form-label">Blood Group</label>
            <input className="form-control" name="blood_group" value={form.blood_group}
                   onChange={handleChange} placeholder="O+" />
          </div>
        )}

        <button className="btn btn-primary w-100" type="submit" disabled={submitting}>
          {submitting ? "Creating account..." : "Sign Up"}
        </button>
      </form>

      <p className="mt-3 text-center">
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
