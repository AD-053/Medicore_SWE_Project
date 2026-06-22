// src/components/Navbar.jsx
//
// SRP: only renders top navigation + logout button. Doesn't know about
// routing logic beyond linking to known paths, doesn't fetch data.

import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar navbar-expand navbar-dark bg-primary px-3">
      <Link className="navbar-brand" to="/">MediCore</Link>
      <div className="ms-auto d-flex align-items-center gap-3">
        {user && (
          <>
            <span className="text-white small">
              {user.name} ({user.role})
            </span>
            <button className="btn btn-sm btn-light" onClick={handleLogout}>
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
