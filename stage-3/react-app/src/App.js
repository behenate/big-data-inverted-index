import React, { useState } from "react";
import axios from "axios";
import "./App.css";

// nginx server ip
const SERVER_IP = "10.195.25.255:3005";

function App() {
  const [word, setWord] = useState("");
  const [data, setData] = useState(null);
  const [author, setAuthor] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [serverId, setServerId] = useState("");

  const fetchData = async () => {
    try {
      console.log(`fetching data for: ${word}`);
      const response = await axios.get(
        `http://${SERVER_IP}/documents/${word}?author=${author}&from=${from}&to=${to}`
      );
      setData(response.data);
      setServerId(response.data.server_id);
    } catch (error) {
      setData(null);
      setServerId("");
    }
  };

  const fetchStats = async (option) => {
    try {
      console.log(`fetching stats with (${option})`);
      const response = await axios.get(`http://${SERVER_IP}/stats/${option}`);
      setData(response.data);
      setServerId(response.data.server_id);
      setWord("");
      setAuthor("");
      setFrom("");
      setTo("");
    } catch (error) {
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
          <div>
            <input
              className="input"
              placeholder="Enter a word"
              type="text"
              value={word}
              onChange={(e) => setWord(e.target.value)}
            />
            <button className="button-1" type="submit">
              Search
            </button>
          </div>
          <div>
            <input
              className="filter-input"
              placeholder="Filter by author"
              type="text"
              value={author}
              onChange={(e) => setAuthor(e.target.value)}
            />
            <input
              className="filter-input"
              placeholder="Filter by min position"
              type="text"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
            />
            <input
              className="filter-input"
              placeholder="Filter by max position"
              type="text"
              value={to}
              onChange={(e) => setTo(e.target.value)}
            />
          </div>
        </form>
        <div>
          <h3>Do you fancy some stats about our data?</h3>
          <div>
            <button className="button-2" onClick={() => fetchStats("words")}>
              How many words?
            </button>
          </div>
        </div>
      </div>
      {serverId ? (
        <h3>Server ID: {serverId}</h3>
      ) : (
        <div style={{ margin: 60 }} />
      )}
      {data && (
        <textarea
          className="results"
          value={
            JSON.stringify(data, null, 2) === "{}"
              ? "Sorry but there are no matching results"
              : JSON.stringify(data, null, 2)
          }
        />
      )}
    </div>
  );
}

export default App;
