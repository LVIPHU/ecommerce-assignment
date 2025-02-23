import '../node_modules/bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import Navbar from "./components/users/Navbar";
import Footer from "./components/users/Footer";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Home from "./components/users/Home";
import Shop from "./components/users/Shop";
import ProductDetail from "./components/users/ProductDetail";

function App() {
    return (

        <BrowserRouter>
            <Navbar/>
            <Routes>
                <Route exact path="/" element={<Home/>}/>
                <Route path="/shop" element={<Shop/>}/>
                <Route path="/product-detail" element={<ProductDetail/>}/>
            </Routes>
            <Footer/>
        </BrowserRouter>


    );
}

export default App;
