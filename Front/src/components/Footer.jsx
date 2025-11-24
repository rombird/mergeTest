import {Link} from 'react-router-dom';
import "../css/common.css";


const Footer = () => {
    return (
        <>
            <footer className="">
                <div className="mainFooter layoutCenter">
                    <div className="footerLogo">
                        <h1>JOB-A-YO</h1>
                    </div>
                    <div className="footerInfo">
                        <div className="footerInfoL">
                            <p>대구광역시 중구 중앙대로 366</p>
                            <p>임과 함께</p>
                            <p>admin@gmail.com</p>
                            <p>053-123-4567</p>
                            <p>JOB-A-YO에서 제공하는 상권정보는 참고 사항이며, 사실과 차이가 있을 수 있고 지연될 수 있습니다.</p>
                        </div>
                        <div className="footerInfoR">
                            <ul className="site">
                                <li><Link className="footer-p" to="/notice" />FAQ</li>
                                <li><Link className="footer-p" to="/sitemap" />사이트맵</li>
                            </ul>
                            <ul className="related">
                                <li><Link className="footer-p" to="/related" />관련기관정보</li>
                            </ul>
                            <ul className="personInfo">
                                <li><Link className="footer-p" to="/personal" />개인정보처리방침</li>
                                <li><Link className="footer-p" to="/use" />이용약관</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </footer>
        
        </>
    )


}
export default Footer;