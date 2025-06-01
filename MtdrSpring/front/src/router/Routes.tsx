/**
 * The Router component sets up application routing using react-router-dom.
 * @returns A JSX element that encapsulates the routing configuration.
 */

import { BrowserRouter, Route, Routes } from "react-router-dom";
import MainPage from "../pages/Main/Main";
import KPIPage from "../pages/Kpi/Kpi";
import ErrorPage from "../pages/Error/Error";
import TeamStats from "../pages/TeamStats/TeamStats";
import LoginPage from "../pages/Login/Login";
import ProtectedRoute from "./ProtectedRoute";
import PendingTasks from "../pages/Pending/Pending";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<MainPage />} />
          <Route path="/kpi" element={<KPIPage />} />
          <Route path="/stats" element={<TeamStats />} />
          <Route path="/pending" element={<PendingTasks />} />
        </Route>
        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;
