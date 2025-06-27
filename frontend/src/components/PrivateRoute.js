import React from "react";
import { Navigate } from "react-route-dom";


const PrivateRoute = ({ children }) => {
    const token = localStorage.getItem("token");
    return token ? children : <Navigate to="/login" replace/>;


};

export default PrivateRoute; 