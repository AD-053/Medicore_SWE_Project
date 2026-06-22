// src/App.jsx
//
// The only file that knows about EVERY page — that's intentional. Routing
// configuration is a legitimate reason for one file to import a lot of
// things; it's still single-responsibility because its one job is
// "map URLs to pages", nothing else.

import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Signup from "./pages/Signup";
import DoctorDirectory from "./pages/patient/DoctorDirectory";

// TODO (Person A): import BookAppointment, Prescriptions, Chat, BloodDonor
// TODO (Person B): import DoctorDashboard, WritePrescription, PatientHistory, ApproveDoctors
// TODO (Person C): import Medicines

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* Patient routes */}
          <Route
            path="/patient"
            element={
              <ProtectedRoute allowedRoles={["patient"]}>
                <DoctorDirectory />
              </ProtectedRoute>
            }
          />

          {/* TODO: add /doctor, /pharmacist, /admin routes the same way,
              each wrapped in ProtectedRoute with its own allowedRoles */}

          <Route path="/" element={<Login />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
