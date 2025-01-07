import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

// nginx server ip
const SERVER_IP = "192.168.1.40:3005"

function App() {
  const [word, setWord] = useState('');
  const [data, setData] = useState(null);
  const [serverId, setServerId] = useState('');

  const fetchData = async () => {
    try {
      const response = await axios.get(`http://${SERVER_IP}/documents/${word}`);
      setData(response.data);
      setServerId(response.data.server_id);
    } catch (error) {
      console.error('Error fetching data:', error);
      setData(null); // clear previous results on error
      setServerId('');
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchData();
  };

  return (
      <div className="App">
        <form onSubmit={handleSubmit}>
          <label>
            Enter a word:
            <input type="text" value={word} onChange={e => setWord(e.target.value)} />
          </label>
          <button type="submit">Search</button>
        </form>
        {serverId && <h2>Server ID: {serverId}</h2>}
        {data && <textarea style={{width: "50vw", height:"60vh"}}>{JSON.stringify(data, null, 2)}</textarea>}
      </div>
  );
}

export default App;