import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout as AntLayout, Menu } from 'antd';
import {
  DashboardOutlined,
  CalendarOutlined,
  FileTextOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { Header, Sider, Content } = AntLayout;

const currentYear = dayjs().year();

const menuItems = [
  {
    key: '/dashboard',
    icon: <DashboardOutlined />,
    label: '数据看板',
  },
  {
    key: `/annual-plan/${currentYear}`,
    icon: <CalendarOutlined />,
    label: '年度规划',
  },
  {
    key: '/monthly-record',
    icon: <FileTextOutlined />,
    label: '月度记录',
  },
  {
    key: `/budget/${currentYear}`,
    icon: <WalletOutlined />,
    label: '预算管理',
  },
];

function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  const getSelectedKey = () => {
    const path = location.pathname;
    if (path.startsWith('/annual-plan')) return `/annual-plan/${currentYear}`;
    if (path.startsWith('/monthly-record')) return '/monthly-record';
    if (path.startsWith('/budget')) return `/budget/${currentYear}`;
    return path;
  };

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ 
          height: 32, 
          margin: 16, 
          background: 'rgba(255,255,255,0.2)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontWeight: 'bold',
        }}>
          {collapsed ? '财' : '家庭财务'}
        </div>
        <Menu
          theme="dark"
          selectedKeys={[getSelectedKey()]}
          mode="inline"
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header style={{ padding: '0 24px', background: '#fff' }}>
          <h2 style={{ margin: 0, lineHeight: '64px' }}>家庭财务管理系统</h2>
        </Header>
        <Content style={{ margin: '16px', padding: 24, background: '#fff', borderRadius: 8 }}>
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
}

export default Layout;
