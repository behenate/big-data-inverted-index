import React, { useState } from "react";
import axios from "axios";
import "./App.css";

// nginx server ip
const SERVER_IP = "192.168.1.139:3005";

function App() {
  const [word, setWord] = useState("");
  const [data, setData] = useState(null);
  const [serverId, setServerId] = useState("");

  const fetchData = async () => {
    try {
      const response = await axios.get(`http://${SERVER_IP}/documents/${word}`);
      setData(response.data);
      setServerId(response.data.server_id);
    } catch (error) {
      console.error("Error fetching data:", error);
      setData(null);
      setServerId("");
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchData();
  };

  return (
    <div className="App">
      <div className="start-page">
        <h1>
          <header className="App-header">Inverted index - stage 3</header>
        </h1>
        <form onSubmit={handleSubmit}>
          <label>
            <input
              className="input"
              placeholder="Enter a word"
              type="text"
              value={word}
              onChange={(e) => setWord(e.target.value)}
            />
          </label>
          <button className="button" type="submit">
            Search
          </button>
        </form>
      </div>
      {serverId && <h3>Server ID: {serverId}</h3>}
      {data && (
        <textarea style={{ width: "50vw", height: "60vh" }}>
          {JSON.stringify(data, null, 2)}
        </textarea>
      )}
    </div>
  );
}

export default App;
