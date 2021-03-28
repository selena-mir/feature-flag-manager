import React from "react";
import ReactDOM from "react-dom";
import {FeatureFlags} from "./FeatureFlags.js";
import "./index.css";

ReactDOM.render(<React.StrictMode>
    <div>
        <div className="header">Feature Flag Manager</div>
        <FeatureFlags/>
    </div>
</React.StrictMode>, document.getElementById("root"));