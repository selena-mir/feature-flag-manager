import React from "react";
import "./FeatureFlags.css";

export class FeatureFlags extends React.Component {
    URL = "http://localhost:8080/featureFlags/";
    regions = [];
    defaultFeatureFlags = [];

    constructor(props) {
        super(props);
        this.state = {
            featureFlags: [],
        };
    }

    componentDidMount() {
        fetch(this.URL).then(this.handleErrorResponse)
            .then(value => value.json())
            .then(data => this.setData(data))
            .catch(() => window.alert("An error occurred during getting data."));
    }

    handleErrorResponse(response) {
        if (!response.ok) {
            throw Error(response.statusText);
        }
        return response;
    }

    setData(data) {
        this.regions = data.regions;
        this.defaultFeatureFlags = this.copyObject(data.features);
        this.setState({featureFlags: data.features});
    }

    copyObject(src) {
        return JSON.parse(JSON.stringify(src));
    }

    render() {
        if (this.state.featureFlags && this.state.featureFlags.length > 0) {
            return <form onSubmit={(e) => this.saveData(e)}>
                <table className="flags-table">
                    <thead className="flags-table_header">
                    <tr>
                        <th>Region</th>
                        {this.regions.map(region => <th key={region}>{region}</th>)}
                    </tr>
                    </thead>
                    <tbody>
                    {this.state.featureFlags.map((feature, featureIndex) => {
                        return <tr key={feature.featureName + "flags"}>
                            <td key={feature.featureName}>{feature.featureName}</td>
                            {this.regions.map(region => <td key={region + feature}>
                                <input type="checkbox"
                                       checked={feature.regionFlags[region]}
                                       onChange={(e) => this.setFlag(featureIndex, region, e.target.checked)}/>
                            </td>)}
                        </tr>;
                    })}
                    </tbody>
                </table>
                <div className="buttons-group">
                    <div>
                        <button className="button" type="button" onClick={() => this.cancelForm()}>Cancel</button>
                        <button className="button" type="submit">Save</button>
                    </div>
                </div>
            </form>;
        }
        return null;
    }

    saveData(e) {
        e.preventDefault();
        fetch(this.URL, {
            method: "POST",
            body: JSON.stringify(this.state.featureFlags),
            headers: {
                "Content-Type": "application/json",
            },
        }).then(this.handleErrorResponse)
            .then(value => value.json())
            .then(data => this.setData(data))
            .catch(() => window.alert("An error occurred. You changes were not saved."));
    }

    setFlag(featureIndex, region, value) {
        this.setState(state => state.featureFlags[featureIndex].regionFlags[region] = value);
    }

    cancelForm() {
        this.setState({
            featureFlags: this.copyObject(this.defaultFeatureFlags),
        });
    }
}